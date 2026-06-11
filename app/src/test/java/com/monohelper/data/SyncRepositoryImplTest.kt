package com.monohelper.data

import com.monohelper.core.result.AppResult
import com.monohelper.data.api.ApiService
import com.monohelper.data.api.BaseUrlInterceptor
import com.monohelper.data.repo.SyncRepositoryImpl
import com.monohelper.domain.model.TaskStatus
import com.monohelper.testutil.FakeApiConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * End-to-end data-layer tests: MockWebServer → Retrofit (NetworkModule-identical setup)
 * → [SyncRepositoryImpl] → domain models / [AppResult].
 */
class SyncRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: SyncRepositoryImpl

    @OptIn(ExperimentalSerializationApi::class)
    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor(FakeApiConfig(server.url("/").toString())))
            .build()
        val api = Retrofit.Builder()
            .baseUrl("http://base-url.invalid/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
        repository = SyncRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getTask maps 404 to Failure with status code in message`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"detail":"Task 99 not found"}"""),
        )

        val result = repository.getTask(99)

        assertTrue(result is AppResult.Failure)
        val message = (result as AppResult.Failure).message
        assertTrue("message should mention 404, was: $message", message.contains("404"))
        assertTrue("message should carry the error detail, was: $message", message.contains("Task 99 not found"))
    }

    @Test
    fun `getTask maps successful task to TaskInfo with result summary`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "id": 42,
                  "type": "sync_transactions",
                  "payload": { "user_id": null, "days": 30 },
                  "status": "success",
                  "result": {
                    "status": "success",
                    "processed_accounts": 4,
                    "total_transactions_synced": 120,
                    "errors": []
                  },
                  "error": null,
                  "attempts": 1,
                  "created_at": "2024-01-01T00:00:00Z",
                  "started_at": "2024-01-01T00:00:01Z",
                  "finished_at": "2024-01-01T00:00:10Z"
                }
                """.trimIndent(),
            ),
        )

        val result = repository.getTask(42)

        assertTrue(result is AppResult.Success)
        val task = (result as AppResult.Success).value
        assertEquals(42L, task.id)
        assertEquals("sync_transactions", task.type)
        assertEquals(TaskStatus.SUCCESS, task.status)
        assertEquals(1, task.attempts)
        val summary = task.resultSummary
        assertTrue("summary should flatten counts, was: $summary", summary?.contains("processed_accounts: 4") == true)
    }
}

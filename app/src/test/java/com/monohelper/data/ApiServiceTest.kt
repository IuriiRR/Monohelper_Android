package com.monohelper.data

import com.monohelper.data.api.ApiService
import com.monohelper.data.api.BaseUrlInterceptor
import com.monohelper.data.api.SyncTransactionsRequestDto
import com.monohelper.testutil.FakeApiConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * MockWebServer tests for the Retrofit stack, built exactly like [com.monohelper.di.NetworkModule]:
 * placeholder base URL + [BaseUrlInterceptor] rewriting every request, kotlinx-serialization
 * converter with `ignoreUnknownKeys` and `explicitNulls = false`.
 */
class ApiServiceTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    /** Mirrors NetworkModule: placeholder base, interceptor-driven base URL, same Json config. */
    @OptIn(ExperimentalSerializationApi::class)
    private fun service(baseUrl: String): ApiService {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(BaseUrlInterceptor(FakeApiConfig(baseUrl)))
            .build()
        return Retrofit.Builder()
            .baseUrl("http://base-url.invalid/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    private fun directService(): ApiService = service(server.url("/").toString())

    private fun gatewayService(): ApiService = service(server.url("/cloudapi").toString())

    @Test
    fun `listAccounts parses accounts payload and hits unprefixed path in direct mode`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "accounts": [
                    {
                      "id": "jar123",
                      "user_id": "user-1",
                      "type": "jar",
                      "send_id": null,
                      "currency_code": 980,
                      "balance": 150000,
                      "is_active": true,
                      "title": "Vacation",
                      "goal": 500000,
                      "is_budget": true,
                      "invested": 0,
                      "created_at": "2024-01-01T00:00:00Z",
                      "updated_at": "2024-01-01T00:00:00Z",
                      "owner_username": "iurii"
                    },
                    {
                      "id": "card456",
                      "user_id": "user-1",
                      "type": "card",
                      "currency_code": 980,
                      "balance": 250000,
                      "is_active": true,
                      "is_budget": false,
                      "invested": 0,
                      "owner_username": null
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )

        val response = directService().listAccounts()

        assertEquals("/accounts/", server.takeRequest().path)
        assertEquals(2, response.accounts.size)
        val jar = response.accounts[0]
        assertEquals("jar123", jar.id)
        assertEquals("user-1", jar.userId)
        assertEquals("jar", jar.type)
        assertEquals(980, jar.currencyCode)
        assertEquals(150_000L, jar.balance)
        assertTrue(jar.isBudget)
        assertEquals("Vacation", jar.title)
        assertEquals(500_000L, jar.goal)
        assertEquals("iurii", jar.ownerUsername)
        val card = response.accounts[1]
        assertEquals("card", card.type)
        assertNull(card.ownerUsername)
    }

    @Test
    fun `gateway mode prepends base path prefix to request path`() = runTest {
        server.enqueue(MockResponse().setBody("""{"accounts": []}"""))

        gatewayService().listAccounts()

        assertEquals("/cloudapi/accounts/", server.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `listTransactions preserves query params under gateway prefix`() = runTest {
        server.enqueue(MockResponse().setBody("""{"transactions": []}"""))

        gatewayService().listTransactions(accountId = "a1", limit = 50)

        val url = server.takeRequest().requestUrl
        assertNotNull(url)
        assertEquals("/cloudapi/transactions/", url?.encodedPath)
        assertEquals("a1", url?.queryParameter("account_id"))
        assertEquals("50", url?.queryParameter("limit"))
        assertNull(url?.queryParameter("user_id"))
    }

    @Test
    fun `monthlyReport sends month query and parses jars with points`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "month": "2024-01",
                  "jars": [
                    {
                      "id": "jar123",
                      "title": "Vacation",
                      "current_balance": 150000,
                      "start_balance": 0,
                      "budget": 500000,
                      "total_deposits": 500000,
                      "spent": -200000,
                      "transactions": [
                        { "time": 1704067200, "balance": 135000 },
                        { "time": 1704153600, "balance": 120000 }
                      ]
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )

        val report = directService().monthlyReport("2024-01")

        val url = server.takeRequest().requestUrl
        assertEquals("/reports/monthly", url?.encodedPath)
        assertEquals("2024-01", url?.queryParameter("month"))
        assertEquals("2024-01", report.month)
        assertEquals(1, report.jars.size)
        val jar = report.jars[0]
        assertEquals("jar123", jar.id)
        assertEquals(500_000L, jar.budget)
        assertEquals(-200_000L, jar.spent)
        assertEquals(2, jar.transactions.size)
        assertEquals(1_704_067_200L, jar.transactions[0].time)
        assertEquals(135_000L, jar.transactions[0].balance)
    }

    @Test
    fun `enqueueTransactionsSync posts days only body when userId is null`() = runTest {
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"task_id": 43, "status": "queued"}"""))

        val response = directService().enqueueTransactionsSync(SyncTransactionsRequestDto(days = 7))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/sync/transactions", request.path)
        // explicitNulls = false → null user_id is omitted entirely.
        assertEquals("""{"days":7}""", request.body.readUtf8())
        assertEquals(43L, response.taskId)
        assertEquals("queued", response.status)
    }

    @Test
    fun `enqueueAccountsSync posts empty object and parses 202 enqueue response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"task_id": 42, "status": "queued"}"""))

        val response = directService().enqueueAccountsSync()

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/sync/accounts", request.path)
        assertEquals("{}", request.body.readUtf8())
        assertEquals(42L, response.taskId)
        assertEquals("queued", response.status)
    }

    @Test
    fun `getTask parses full task object including result JsonObject`() = runTest {
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

        val task = directService().getTask(42)

        assertEquals("/tasks/42", server.takeRequest().path)
        assertEquals(42L, task.id)
        assertEquals("sync_transactions", task.type)
        assertEquals("success", task.status)
        assertNull(task.error)
        assertEquals(1, task.attempts)
        val result = task.result
        assertNotNull(result)
        assertEquals("success", result?.get("status")?.jsonPrimitive?.content)
        assertEquals(4, result?.get("processed_accounts")?.jsonPrimitive?.int)
        assertEquals(0, result?.get("errors")?.jsonArray?.size)
    }

    @Test
    fun `healthz parses heartbeat payload with null last_error`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "status": "ok",
                  "last_heartbeat_at": "2024-01-01T00:00:00Z",
                  "last_error": null
                }
                """.trimIndent(),
            ),
        )

        val health = directService().healthz()

        assertEquals("/healthz", server.takeRequest().path)
        assertEquals("ok", health.status)
        assertEquals("2024-01-01T00:00:00Z", health.lastHeartbeatAt)
        assertNull(health.lastError)
    }
}

package com.monohelper.data

import com.monohelper.data.api.AccountDto
import com.monohelper.data.api.HealthzDto
import com.monohelper.data.api.TaskDto
import com.monohelper.data.repo.toDomain
import com.monohelper.domain.model.AccountType
import com.monohelper.domain.model.TaskStatus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure DTO → domain mapper tests (mappers are internal, accessible from this module's tests). */
class MappersTest {

    private fun accountDto(type: String) = AccountDto(id = "acc-1", userId = "user-1", type = type)

    private fun taskDto(
        status: String = "success",
        result: JsonObject? = null,
    ) = TaskDto(id = 1, type = "sync_accounts", status = status, result = result)

    @Test
    fun `account type card maps to CARD`() {
        assertEquals(AccountType.CARD, accountDto("card").toDomain().type)
    }

    @Test
    fun `account type jar maps to JAR`() {
        assertEquals(AccountType.JAR, accountDto("jar").toDomain().type)
    }

    @Test
    fun `unknown account type maps to OTHER`() {
        assertEquals(AccountType.OTHER, accountDto("weird").toDomain().type)
    }

    @Test
    fun `task status strings map to TaskStatus values`() {
        assertEquals(TaskStatus.PENDING, taskDto(status = "pending").toDomain().status)
        assertEquals(TaskStatus.RUNNING, taskDto(status = "running").toDomain().status)
        assertEquals(TaskStatus.SUCCESS, taskDto(status = "success").toDomain().status)
        assertEquals(TaskStatus.ERROR, taskDto(status = "error").toDomain().status)
        assertEquals(TaskStatus.UNKNOWN, taskDto(status = "exploded").toDomain().status)
    }

    @Test
    fun `task result flattens to summary lines with empty array as none`() {
        val result = buildJsonObject {
            put("status", "success")
            put("processed_accounts", 4)
            put("errors", buildJsonArray { })
        }

        val summary = taskDto(result = result).toDomain().resultSummary

        assertTrue("was: $summary", summary?.contains("processed_accounts: 4") == true)
        assertTrue("was: $summary", summary?.contains("errors: none") == true)
    }

    @Test
    fun `task result joins error strings with semicolons`() {
        val result = buildJsonObject {
            put("status", "error")
            put(
                "errors",
                buildJsonArray {
                    add("boom")
                    add("bang")
                },
            )
        }

        val summary = taskDto(result = result).toDomain().resultSummary

        assertTrue("was: $summary", summary?.contains("errors: boom; bang") == true)
    }

    @Test
    fun `task without result has null summary`() {
        assertNull(taskDto(result = null).toDomain().resultSummary)
    }

    @Test
    fun `healthz ok status with no error maps to healthy`() {
        val health = HealthzDto(status = "ok", lastHeartbeatAt = "2024-01-01T00:00:00Z", lastError = null).toDomain()

        assertTrue(health.ok)
        assertEquals("2024-01-01T00:00:00Z", health.lastHeartbeatAt)
        assertNull(health.lastError)
    }

    @Test
    fun `healthz with last error maps to unhealthy`() {
        val health = HealthzDto(status = "ok", lastError = "worker crashed").toDomain()

        assertFalse(health.ok)
        assertEquals("worker crashed", health.lastError)
    }
}

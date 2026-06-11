package com.monohelper.domain.usecase

import app.cash.turbine.test
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.TaskStatus
import com.monohelper.testutil.FakeSyncRepository
import com.monohelper.testutil.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PollTaskTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `emits every observed state until terminal success`() = runTest {
        val repo = FakeSyncRepository()
        repo.taskResults.addAll(
            listOf(
                AppResult.Success(TestData.taskInfo(status = TaskStatus.PENDING)),
                AppResult.Success(TestData.taskInfo(status = TaskStatus.RUNNING)),
                AppResult.Success(TestData.taskInfo(status = TaskStatus.SUCCESS)),
            ),
        )
        val pollTask = PollTask(repo)

        pollTask(taskId = 1).test {
            val first = awaitItem()
            assertTrue(first is AppResult.Success)
            assertEquals(TaskStatus.PENDING, (first as AppResult.Success).value.status)

            val second = awaitItem()
            assertTrue(second is AppResult.Success)
            assertEquals(TaskStatus.RUNNING, (second as AppResult.Success).value.status)

            val third = awaitItem()
            assertTrue(third is AppResult.Success)
            assertEquals(TaskStatus.SUCCESS, (third as AppResult.Success).value.status)

            awaitComplete()
        }

        assertEquals(2 * PollTask.DEFAULT_POLL_INTERVAL_MS, currentTime)
        assertEquals(3, repo.getTaskCalls)
    }

    @Test
    fun `failures below threshold are silent before final success`() = runTest {
        val repo = FakeSyncRepository()
        repo.taskResults.addAll(
            listOf(
                AppResult.Failure("network down"),
                AppResult.Failure("network still down"),
                AppResult.Success(TestData.taskInfo(status = TaskStatus.SUCCESS)),
            ),
        )
        val pollTask = PollTask(repo)

        pollTask(taskId = 1).test {
            val only = awaitItem()
            assertTrue(only is AppResult.Success)
            assertEquals(TaskStatus.SUCCESS, (only as AppResult.Success).value.status)
            awaitComplete()
        }

        assertEquals(3, repo.getTaskCalls)
    }

    @Test
    fun `three consecutive failures emit one failure then complete`() = runTest {
        val repo = FakeSyncRepository()
        repo.taskResults.addAll(
            listOf(
                AppResult.Failure("e1"),
                AppResult.Failure("e2"),
                AppResult.Failure("e3"),
            ),
        )
        repo.fallbackTask = AppResult.Failure("still down")
        val pollTask = PollTask(repo)

        pollTask(taskId = 1).test {
            val failure = awaitItem()
            assertTrue(failure is AppResult.Failure)
            assertEquals("e3", (failure as AppResult.Failure).message)
            awaitComplete()
        }

        assertEquals(3, repo.getTaskCalls)
    }
}

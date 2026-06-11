package com.monohelper.ui.sync

import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.usecase.EnqueueSync
import com.monohelper.domain.usecase.PollTask
import com.monohelper.domain.usecase.SyncKind
import com.monohelper.testutil.FakeSyncRepository
import com.monohelper.testutil.MainDispatcherRule
import com.monohelper.testutil.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeSyncRepository) = SyncViewModel(
        enqueueSync = EnqueueSync(repository),
        pollTask = PollTask(repository),
    )

    @Test
    fun `start accounts polls to finished success`() = runTest {
        val repository = FakeSyncRepository()
        repository.taskResults.addLast(AppResult.Success(TestData.taskInfo(status = TaskStatus.RUNNING)))
        repository.taskResults.addLast(AppResult.Success(TestData.taskInfo(status = TaskStatus.SUCCESS)))
        val viewModel = viewModel(repository)

        viewModel.start(SyncKind.ACCOUNTS)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is SyncRunState.Finished)
        state as SyncRunState.Finished
        assertEquals(SyncKind.ACCOUNTS, state.kind)
        assertEquals(TaskStatus.SUCCESS, state.task.status)
        assertEquals(1, repository.enqueueAccountsCalls)
    }

    @Test
    fun `enqueue failure surfaces failed state`() = runTest {
        val repository = FakeSyncRepository()
        repository.enqueueResult = AppResult.Failure("server unreachable")
        val viewModel = viewModel(repository)

        viewModel.start(SyncKind.ACCOUNTS)
        advanceUntilIdle()

        assertEquals(
            SyncRunState.Failed(SyncKind.ACCOUNTS, "server unreachable"),
            viewModel.state.value,
        )
    }

    @Test
    fun `transactions kind calls enqueueTransactionsSync`() = runTest {
        val repository = FakeSyncRepository()
        val viewModel = viewModel(repository)

        viewModel.start(SyncKind.TRANSACTIONS)
        advanceUntilIdle()

        assertEquals(1, repository.enqueueTransactionsCalls)
        assertEquals(0, repository.enqueueAccountsCalls)
        assertTrue(viewModel.state.value is SyncRunState.Finished)
    }

    @Test
    fun `start while polling is ignored`() = runTest {
        val repository = FakeSyncRepository()
        repository.taskResults.addLast(AppResult.Success(TestData.taskInfo(status = TaskStatus.RUNNING)))
        val viewModel = viewModel(repository)

        viewModel.start(SyncKind.ACCOUNTS)
        assertTrue(viewModel.state.value is SyncRunState.Polling)

        viewModel.start(SyncKind.ACCOUNTS)
        advanceUntilIdle()

        assertEquals(1, repository.enqueueAccountsCalls)
        assertTrue(viewModel.state.value is SyncRunState.Finished)
    }
}

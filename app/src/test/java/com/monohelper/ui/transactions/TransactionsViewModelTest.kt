package com.monohelper.ui.transactions

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.TransactionFilter
import com.monohelper.domain.usecase.ListTransactions
import com.monohelper.testutil.FakeTransactionRepository
import com.monohelper.testutil.MainDispatcherRule
import com.monohelper.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fake = FakeTransactionRepository()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        TransactionsViewModel(
            listTransactions = ListTransactions(fake),
            savedStateHandle = savedStateHandle,
        )

    @Test
    fun accountIdArgumentDrivesFilter() = runTest {
        val vm = viewModel(SavedStateHandle(mapOf("accountId" to "acc-9")))

        assertEquals("acc-9", vm.accountId)
        assertEquals(TransactionFilter(accountId = "acc-9"), fake.lastFilter)
    }

    @Test
    fun emptySavedStateHandleUsesNullAccountId() = runTest {
        val vm = viewModel()

        assertNull(vm.accountId)
        assertEquals(TransactionFilter(accountId = null), fake.lastFilter)
    }

    @Test
    fun refreshSuccessExposesTransactions() = runTest {
        val transactions = listOf(TestData.transaction())
        fake.refreshResult = AppResult.Success(transactions)

        val vm = viewModel()

        vm.state.test {
            assertEquals(TransactionsUiState.Success(transactions), awaitItem())
        }
    }

    @Test
    fun refreshFailureExposesError() = runTest {
        fake.refreshResult = AppResult.Failure("network down")

        val vm = viewModel()

        vm.state.test {
            assertEquals(TransactionsUiState.Error("network down"), awaitItem())
        }
    }
}

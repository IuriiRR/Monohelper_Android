package com.monohelper.ui.accounts

import app.cash.turbine.test
import com.monohelper.core.result.AppResult
import com.monohelper.domain.usecase.ListAccounts
import com.monohelper.testutil.FakeAccountRepository
import com.monohelper.testutil.MainDispatcherRule
import com.monohelper.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccountsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fake = FakeAccountRepository()

    private fun viewModel() = AccountsViewModel(ListAccounts(fake))

    @Test
    fun initTriggersRefreshAndReachesSuccess() = runTest {
        val accounts = listOf(TestData.account())
        fake.refreshResult = AppResult.Success(accounts)

        val vm = viewModel()

        vm.state.test {
            assertEquals(AccountsUiState.Success(accounts), awaitItem())
        }
        assertEquals(1, fake.refreshCalls)
    }

    @Test
    fun refreshFailureWithoutCacheEmitsError() = runTest {
        fake.refreshResult = AppResult.Failure("network down")

        val vm = viewModel()

        vm.state.test {
            assertEquals(AccountsUiState.Error("network down"), awaitItem())
        }
        assertEquals(1, fake.refreshCalls)
    }

    @Test
    fun seededCacheEmitsSuccessFirstEvenWhenRefreshFails() = runTest {
        val cached = listOf(TestData.account())
        fake.seed(cached)
        fake.refreshResult = AppResult.Failure("network down")

        val vm = viewModel()

        vm.state.test {
            assertEquals(AccountsUiState.Success(cached), awaitItem())
        }
        assertEquals(1, fake.refreshCalls)
    }
}

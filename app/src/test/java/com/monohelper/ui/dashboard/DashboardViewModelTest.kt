package com.monohelper.ui.dashboard

import app.cash.turbine.test
import com.monohelper.core.result.AppResult
import com.monohelper.domain.usecase.GetMonthlyReport
import com.monohelper.testutil.FakeReportRepository
import com.monohelper.testutil.MainDispatcherRule
import com.monohelper.testutil.TestData
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(fake: FakeReportRepository): DashboardViewModel =
        DashboardViewModel(GetMonthlyReport(fake))

    @Test
    fun initLoadsCurrentMonth() = runTest {
        val fake = FakeReportRepository()
        val report = TestData.monthlyReport()
        fake.refreshResult = AppResult.Success(report)

        val vm = viewModel(fake)

        assertEquals(YearMonth.now().toString(), fake.lastMonth)
        vm.state.test {
            assertEquals(DashboardUiState.Success(report), awaitItem())
        }
    }

    @Test
    fun failureBecomesError() = runTest {
        val fake = FakeReportRepository()
        fake.refreshResult = AppResult.Failure("boom")

        val vm = viewModel(fake)

        vm.state.test {
            assertEquals(DashboardUiState.Error("boom"), awaitItem())
        }
    }

    @Test
    fun previousMonthReloadsPreviousMonth() = runTest {
        val fake = FakeReportRepository()
        val vm = viewModel(fake)

        vm.previousMonth()

        assertEquals(YearMonth.now().minusMonths(1).toString(), fake.lastMonth)
        assertEquals(2, fake.refreshCalls)
        assertEquals(YearMonth.now().minusMonths(1), vm.month.value)
    }

    @Test
    fun nextMonthAtCurrentMonthIsNoOp() = runTest {
        val fake = FakeReportRepository()
        val vm = viewModel(fake)

        vm.nextMonth()

        assertEquals(1, fake.refreshCalls)
        assertEquals(YearMonth.now(), vm.month.value)
    }
}

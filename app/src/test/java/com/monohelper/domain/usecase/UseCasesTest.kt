package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.model.TransactionFilter
import com.monohelper.testutil.FakeAccountRepository
import com.monohelper.testutil.FakeReportRepository
import com.monohelper.testutil.FakeTransactionRepository
import com.monohelper.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {

    @Test
    fun `listAccounts delegates to refresh and updates cache`() = runTest {
        val repo = FakeAccountRepository()
        val accounts = listOf(TestData.account())
        repo.refreshResult = AppResult.Success(accounts)
        val listAccounts = ListAccounts(repo)

        assertNull(listAccounts.cached.value)
        val result = listAccounts()

        assertEquals(AppResult.Success(accounts), result)
        assertEquals(1, repo.refreshCalls)
        assertEquals(accounts, listAccounts.cached.value)
    }

    @Test
    fun `listTransactions caches per filter`() = runTest {
        val repo = FakeTransactionRepository()
        val transactions = listOf(TestData.transaction())
        repo.refreshResult = AppResult.Success(transactions)
        val listTransactions = ListTransactions(repo)
        val filter = TransactionFilter(accountId = "acc-1")

        val result = listTransactions(filter)

        assertEquals(AppResult.Success(transactions), result)
        assertEquals(transactions, listTransactions.cached(filter))
        assertNull(listTransactions.cached(TransactionFilter(accountId = "acc-2")))
    }

    @Test
    fun `getMonthlyReport caches by month and passes userId`() = runTest {
        val repo = FakeReportRepository()
        val report = TestData.monthlyReport(month = "2024-01")
        repo.refreshResult = AppResult.Success(report)
        val getMonthlyReport = GetMonthlyReport(repo)

        assertNull(getMonthlyReport.cached("2024-01"))
        getMonthlyReport("2024-01", userId = "user-9")

        assertEquals(report, getMonthlyReport.cached("2024-01"))
        assertEquals("2024-01", repo.lastMonth)
        assertEquals("user-9", repo.lastUserId)
    }

    @Test
    fun `taskStatus from is case insensitive`() {
        assertEquals(TaskStatus.RUNNING, TaskStatus.from("running"))
        assertEquals(TaskStatus.ERROR, TaskStatus.from("ERROR"))
        assertEquals(TaskStatus.UNKNOWN, TaskStatus.from("nope"))
    }

    @Test
    fun `taskStatus terminal flags`() {
        assertTrue(TaskStatus.SUCCESS.isTerminal)
        assertTrue(TaskStatus.ERROR.isTerminal)
        assertFalse(TaskStatus.PENDING.isTerminal)
        assertFalse(TaskStatus.RUNNING.isTerminal)
        assertFalse(TaskStatus.UNKNOWN.isTerminal)
    }
}

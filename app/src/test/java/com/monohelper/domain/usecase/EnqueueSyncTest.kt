package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.testutil.FakeSyncRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnqueueSyncTest {

    @Test
    fun `accounts kind routes to accounts sync only`() = runTest {
        val repo = FakeSyncRepository()
        val enqueueSync = EnqueueSync(repo)

        val result = enqueueSync(SyncKind.ACCOUNTS)

        assertTrue(result is AppResult.Success)
        assertEquals(1, repo.enqueueAccountsCalls)
        assertEquals(0, repo.enqueueTransactionsCalls)
    }

    @Test
    fun `transactions kind routes to transactions sync with days`() = runTest {
        val repo = FakeSyncRepository()
        val enqueueSync = EnqueueSync(repo)

        val result = enqueueSync(SyncKind.TRANSACTIONS, days = 14)

        assertTrue(result is AppResult.Success)
        assertEquals(0, repo.enqueueAccountsCalls)
        assertEquals(1, repo.enqueueTransactionsCalls)
        assertEquals(14, repo.lastDays)
    }
}

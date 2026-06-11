package com.monohelper.testutil

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.AccountRepository
import com.monohelper.data.repo.ReportRepository
import com.monohelper.data.repo.SyncRepository
import com.monohelper.data.repo.TransactionRepository
import com.monohelper.domain.model.Account
import com.monohelper.domain.model.EnqueuedTask
import com.monohelper.domain.model.MonthlyReport
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.model.Transaction
import com.monohelper.domain.model.TransactionFilter
import com.monohelper.domain.model.WorkerHealth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Fake repository implementations for ViewModel tests — no network anywhere.

class FakeAccountRepository : AccountRepository {
    var refreshResult: AppResult<List<Account>> = AppResult.Success(emptyList())
    var refreshCalls = 0
        private set
    var lastUserId: String? = null
        private set

    private val _accounts = MutableStateFlow<List<Account>?>(null)
    override val accounts: StateFlow<List<Account>?> = _accounts.asStateFlow()

    override suspend fun refresh(userId: String?): AppResult<List<Account>> {
        refreshCalls++
        lastUserId = userId
        val result = refreshResult
        if (result is AppResult.Success) _accounts.value = result.value
        return result
    }

    fun seed(accounts: List<Account>) {
        _accounts.value = accounts
    }
}

class FakeTransactionRepository : TransactionRepository {
    var refreshResult: AppResult<List<Transaction>> = AppResult.Success(emptyList())
    var refreshCalls = 0
        private set
    var lastFilter: TransactionFilter? = null
        private set

    private val _cache = MutableStateFlow<Map<TransactionFilter, List<Transaction>>>(emptyMap())
    override val cache: StateFlow<Map<TransactionFilter, List<Transaction>>> = _cache.asStateFlow()

    override suspend fun refresh(filter: TransactionFilter): AppResult<List<Transaction>> {
        refreshCalls++
        lastFilter = filter
        val result = refreshResult
        if (result is AppResult.Success) _cache.value = _cache.value + (filter to result.value)
        return result
    }

    fun seed(filter: TransactionFilter, transactions: List<Transaction>) {
        _cache.value = _cache.value + (filter to transactions)
    }
}

class FakeReportRepository : ReportRepository {
    var refreshResult: AppResult<MonthlyReport> = AppResult.Success(TestData.monthlyReport())
    var refreshCalls = 0
        private set
    var lastMonth: String? = null
        private set
    var lastUserId: String? = null
        private set

    private val _reports = MutableStateFlow<Map<String, MonthlyReport>>(emptyMap())
    override val reports: StateFlow<Map<String, MonthlyReport>> = _reports.asStateFlow()

    override suspend fun refresh(month: String, userId: String?): AppResult<MonthlyReport> {
        refreshCalls++
        lastMonth = month
        lastUserId = userId
        val result = refreshResult
        if (result is AppResult.Success) _reports.value = _reports.value + (month to result.value)
        return result
    }
}

class FakeSyncRepository : SyncRepository {
    var enqueueResult: AppResult<EnqueuedTask> = AppResult.Success(EnqueuedTask(taskId = 1, status = "queued"))
    var enqueueAccountsCalls = 0
        private set
    var enqueueTransactionsCalls = 0
        private set
    var lastDays: Int? = null
        private set
    var getTaskCalls = 0
        private set

    /** Consumed first, in order; when empty [fallbackTask] is returned. */
    val taskResults = ArrayDeque<AppResult<TaskInfo>>()
    var fallbackTask: AppResult<TaskInfo> = AppResult.Success(TestData.taskInfo(status = TaskStatus.SUCCESS))
    var healthResult: AppResult<WorkerHealth> =
        AppResult.Success(WorkerHealth(ok = true, lastHeartbeatAt = null, lastError = null))

    override suspend fun enqueueAccountsSync(): AppResult<EnqueuedTask> {
        enqueueAccountsCalls++
        return enqueueResult
    }

    override suspend fun enqueueTransactionsSync(userId: String?, days: Int?): AppResult<EnqueuedTask> {
        enqueueTransactionsCalls++
        lastDays = days
        return enqueueResult
    }

    override suspend fun getTask(taskId: Long): AppResult<TaskInfo> {
        getTaskCalls++
        return taskResults.removeFirstOrNull() ?: fallbackTask
    }

    override suspend fun workerHealth(): AppResult<WorkerHealth> = healthResult
}

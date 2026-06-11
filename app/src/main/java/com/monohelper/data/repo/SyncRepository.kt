package com.monohelper.data.repo

import com.monohelper.core.result.AppResult
import com.monohelper.data.api.ApiService
import com.monohelper.data.api.SyncTransactionsRequestDto
import com.monohelper.domain.model.EnqueuedTask
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.model.WorkerHealth
import javax.inject.Inject
import javax.inject.Singleton

interface SyncRepository {
    suspend fun enqueueAccountsSync(): AppResult<EnqueuedTask>
    suspend fun enqueueTransactionsSync(userId: String? = null, days: Int? = null): AppResult<EnqueuedTask>
    suspend fun getTask(taskId: Long): AppResult<TaskInfo>
    suspend fun workerHealth(): AppResult<WorkerHealth>
}

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : SyncRepository {

    override suspend fun enqueueAccountsSync(): AppResult<EnqueuedTask> = safeCall {
        val dto = api.enqueueAccountsSync()
        EnqueuedTask(taskId = dto.taskId, status = dto.status)
    }

    override suspend fun enqueueTransactionsSync(userId: String?, days: Int?): AppResult<EnqueuedTask> = safeCall {
        val dto = api.enqueueTransactionsSync(SyncTransactionsRequestDto(userId = userId, days = days))
        EnqueuedTask(taskId = dto.taskId, status = dto.status)
    }

    override suspend fun getTask(taskId: Long): AppResult<TaskInfo> = safeCall {
        api.getTask(taskId).toDomain()
    }

    override suspend fun workerHealth(): AppResult<WorkerHealth> = safeCall {
        api.healthz().toDomain()
    }
}

package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.SyncRepository
import com.monohelper.domain.model.WorkerHealth
import javax.inject.Inject

class GetWorkerHealth @Inject constructor(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(): AppResult<WorkerHealth> = repository.workerHealth()
}

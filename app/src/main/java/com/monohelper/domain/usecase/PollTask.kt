package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.SyncRepository
import com.monohelper.domain.model.TaskInfo
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Polls `GET /tasks/{id}` until the task reaches a terminal status.
 *
 * Emits every observed state. Transient fetch failures are tolerated up to
 * [maxConsecutiveFailures] in a row, then surfaced and the flow completes.
 * The interval is injected per call so tests run on virtual time.
 */
class PollTask @Inject constructor(
    private val repository: SyncRepository,
) {
    operator fun invoke(
        taskId: Long,
        pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
        maxConsecutiveFailures: Int = DEFAULT_MAX_CONSECUTIVE_FAILURES,
    ): Flow<AppResult<TaskInfo>> = flow {
        var consecutiveFailures = 0
        while (true) {
            when (val result = repository.getTask(taskId)) {
                is AppResult.Success -> {
                    consecutiveFailures = 0
                    emit(result)
                    if (result.value.status.isTerminal) return@flow
                }
                is AppResult.Failure -> {
                    consecutiveFailures++
                    if (consecutiveFailures >= maxConsecutiveFailures) {
                        emit(result)
                        return@flow
                    }
                }
            }
            delay(pollIntervalMs)
        }
    }

    companion object {
        const val DEFAULT_POLL_INTERVAL_MS = 1_500L
        const val DEFAULT_MAX_CONSECUTIVE_FAILURES = 3
    }
}

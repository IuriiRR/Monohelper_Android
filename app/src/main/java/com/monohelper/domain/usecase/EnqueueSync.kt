package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.SyncRepository
import com.monohelper.domain.model.EnqueuedTask
import javax.inject.Inject

enum class SyncKind { ACCOUNTS, TRANSACTIONS }

class EnqueueSync @Inject constructor(
    private val repository: SyncRepository,
) {
    suspend operator fun invoke(kind: SyncKind, days: Int? = null): AppResult<EnqueuedTask> =
        when (kind) {
            SyncKind.ACCOUNTS -> repository.enqueueAccountsSync()
            SyncKind.TRANSACTIONS -> repository.enqueueTransactionsSync(days = days)
        }
}

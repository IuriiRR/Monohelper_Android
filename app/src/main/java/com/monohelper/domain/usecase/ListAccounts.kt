package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.AccountRepository
import com.monohelper.domain.model.Account
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ListAccounts @Inject constructor(
    private val repository: AccountRepository,
) {
    /** Repository cache; `null` until the first successful refresh. */
    val cached: StateFlow<List<Account>?> get() = repository.accounts

    suspend operator fun invoke(userId: String? = null): AppResult<List<Account>> =
        repository.refresh(userId)
}

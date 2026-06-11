package com.monohelper.data.repo

import com.monohelper.core.result.AppResult
import com.monohelper.data.api.ApiService
import com.monohelper.domain.model.Account
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AccountRepository {
    /** Last successfully fetched list; `null` until the first refresh. */
    val accounts: StateFlow<List<Account>?>

    suspend fun refresh(userId: String? = null): AppResult<List<Account>>
}

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : AccountRepository {

    private val _accounts = MutableStateFlow<List<Account>?>(null)
    override val accounts: StateFlow<List<Account>?> = _accounts.asStateFlow()

    override suspend fun refresh(userId: String?): AppResult<List<Account>> {
        val result = safeCall { api.listAccounts(userId).accounts.map { it.toDomain() } }
        if (result is AppResult.Success) _accounts.value = result.value
        return result
    }
}

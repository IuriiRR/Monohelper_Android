package com.monohelper.data.repo

import com.monohelper.core.result.AppResult
import com.monohelper.data.api.ApiService
import com.monohelper.domain.model.Transaction
import com.monohelper.domain.model.TransactionFilter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TransactionRepository {
    /** Per-filter cache of the last successful fetch. */
    val cache: StateFlow<Map<TransactionFilter, List<Transaction>>>

    suspend fun refresh(filter: TransactionFilter = TransactionFilter()): AppResult<List<Transaction>>
}

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : TransactionRepository {

    private val _cache = MutableStateFlow<Map<TransactionFilter, List<Transaction>>>(emptyMap())
    override val cache: StateFlow<Map<TransactionFilter, List<Transaction>>> = _cache.asStateFlow()

    override suspend fun refresh(filter: TransactionFilter): AppResult<List<Transaction>> {
        val result = safeCall {
            api.listTransactions(
                userId = filter.userId,
                accountId = filter.accountId,
                limit = filter.limit,
            ).transactions.map { it.toDomain() }
        }
        if (result is AppResult.Success) {
            _cache.value = _cache.value + (filter to result.value)
        }
        return result
    }
}

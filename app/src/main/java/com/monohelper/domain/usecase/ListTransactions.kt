package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.TransactionRepository
import com.monohelper.domain.model.Transaction
import com.monohelper.domain.model.TransactionFilter
import javax.inject.Inject

class ListTransactions @Inject constructor(
    private val repository: TransactionRepository,
) {
    fun cached(filter: TransactionFilter): List<Transaction>? =
        repository.cache.value[filter]

    suspend operator fun invoke(filter: TransactionFilter = TransactionFilter()): AppResult<List<Transaction>> =
        repository.refresh(filter)
}

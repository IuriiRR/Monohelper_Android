package com.monohelper.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.Transaction
import com.monohelper.domain.model.TransactionFilter
import com.monohelper.domain.usecase.ListTransactions
import com.monohelper.ui.nav.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TransactionsUiState {
    data object Loading : TransactionsUiState
    data class Success(val transactions: List<Transaction>) : TransactionsUiState
    data class Error(val message: String) : TransactionsUiState
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val listTransactions: ListTransactions,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Optional account drill-down argument from the navigation route. */
    val accountId: String? = savedStateHandle[Routes.ARG_ACCOUNT_ID]

    private val filter = TransactionFilter(accountId = accountId)

    private val _state = MutableStateFlow<TransactionsUiState>(TransactionsUiState.Loading)
    val state: StateFlow<TransactionsUiState> = _state.asStateFlow()

    init {
        listTransactions.cached(filter)?.let { cached ->
            _state.value = TransactionsUiState.Success(cached)
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_state.value !is TransactionsUiState.Success) {
                _state.value = TransactionsUiState.Loading
            }
            _state.value = when (val result = listTransactions(filter)) {
                is AppResult.Success -> TransactionsUiState.Success(result.value)
                is AppResult.Failure -> TransactionsUiState.Error(result.message)
            }
        }
    }
}

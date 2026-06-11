package com.monohelper.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.Account
import com.monohelper.domain.usecase.ListAccounts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Success(val accounts: List<Account>) : AccountsUiState
    data class Error(val message: String) : AccountsUiState
}

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val listAccounts: ListAccounts,
) : ViewModel() {

    private val _state = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val state: StateFlow<AccountsUiState> = _state.asStateFlow()

    init {
        val cached = listAccounts.cached.value
        if (cached != null) {
            _state.value = AccountsUiState.Success(cached)
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            when (val result = listAccounts()) {
                is AppResult.Success -> _state.value = AccountsUiState.Success(result.value)
                is AppResult.Failure -> {
                    val cached = listAccounts.cached.value
                    if (cached == null) {
                        _state.value = AccountsUiState.Error(result.message)
                    } else {
                        // Keep showing cached accounts; a stale list beats an error screen.
                        _state.value = AccountsUiState.Success(cached)
                    }
                }
            }
        }
    }
}

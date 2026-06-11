package com.monohelper.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.MonthlyReport
import com.monohelper.domain.usecase.GetMonthlyReport
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val report: MonthlyReport) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getMonthlyReport: GetMonthlyReport,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
        refresh()
    }

    fun nextMonth() {
        if (_month.value >= YearMonth.now()) return
        _month.value = _month.value.plusMonths(1)
        refresh()
    }

    fun refresh() {
        val monthStr = _month.value.toString()
        val cached = getMonthlyReport.cached(monthStr)
        _state.value = if (cached != null) {
            DashboardUiState.Success(cached)
        } else {
            DashboardUiState.Loading
        }
        viewModelScope.launch {
            val result = getMonthlyReport(monthStr)
            if (_month.value.toString() != monthStr) return@launch
            when (result) {
                is AppResult.Success -> _state.value = DashboardUiState.Success(result.value)
                is AppResult.Failure -> if (cached == null) {
                    _state.value = DashboardUiState.Error(result.message)
                }
            }
        }
    }
}

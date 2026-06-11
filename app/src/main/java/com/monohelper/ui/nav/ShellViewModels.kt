package com.monohelper.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monohelper.core.config.ApiConfig
import com.monohelper.core.result.AppResult
import com.monohelper.domain.model.WorkerHealth
import com.monohelper.domain.usecase.GetWorkerHealth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiConfig: ApiConfig,
) : ViewModel() {
    val baseUrl: StateFlow<String> = apiConfig.baseUrl

    fun save(url: String) {
        apiConfig.setBaseUrl(url)
    }
}

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val getWorkerHealth: GetWorkerHealth,
) : ViewModel() {

    private val _health = MutableStateFlow<AppResult<WorkerHealth>?>(null)
    val health: StateFlow<AppResult<WorkerHealth>?> = _health.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _health.value = getWorkerHealth()
                delay(HEALTH_REFRESH_MS)
            }
        }
    }

    companion object {
        const val HEALTH_REFRESH_MS = 30_000L
    }
}

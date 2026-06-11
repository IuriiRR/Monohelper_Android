package com.monohelper.testutil

import com.monohelper.core.config.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory [ApiConfig] for data-layer tests — no persistence, no Android types. */
class FakeApiConfig(initial: String) : ApiConfig {

    private val _baseUrl = MutableStateFlow(initial)
    override val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    override fun setBaseUrl(url: String) {
        _baseUrl.value = url
    }
}

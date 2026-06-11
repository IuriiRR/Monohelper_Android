package com.monohelper.core.config

import kotlinx.coroutines.flow.StateFlow

/**
 * Provider of the configurable backend base URL.
 *
 * The backend is reachable two ways (direct `http://<pi>:8088`, gateway
 * `http://<host>:8888/cloudapi`); the app treats the base URL as one opaque,
 * user-editable string. All API paths are relative to it.
 */
interface ApiConfig {
    val baseUrl: StateFlow<String>
    fun setBaseUrl(url: String)
}

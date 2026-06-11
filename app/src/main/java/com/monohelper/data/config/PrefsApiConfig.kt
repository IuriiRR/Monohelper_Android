package com.monohelper.data.config

import android.content.Context
import com.monohelper.BuildConfig
import com.monohelper.core.config.ApiConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** [ApiConfig] backed by SharedPreferences; survives app restarts. */
@Singleton
class PrefsApiConfig @Inject constructor(
    @ApplicationContext context: Context,
) : ApiConfig {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _baseUrl = MutableStateFlow(
        prefs.getString(KEY_BASE_URL, null) ?: BuildConfig.DEFAULT_BASE_URL,
    )
    override val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    override fun setBaseUrl(url: String) {
        val cleaned = url.trim().trimEnd('/')
        if (cleaned.isEmpty()) return
        prefs.edit().putString(KEY_BASE_URL, cleaned).apply()
        _baseUrl.value = cleaned
    }

    private companion object {
        const val PREFS_NAME = "api_config"
        const val KEY_BASE_URL = "base_url"
    }
}

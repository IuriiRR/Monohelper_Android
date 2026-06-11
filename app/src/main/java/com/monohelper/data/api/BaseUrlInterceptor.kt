package com.monohelper.data.api

import com.monohelper.core.config.ApiConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites every request against the user-configured base URL at call time.
 *
 * Retrofit is built with a placeholder base; this interceptor swaps in the
 * scheme/host/port and prepends the base path (e.g. `/cloudapi`) so the same
 * build works in direct and gateway modes without hardcoding either.
 */
class BaseUrlInterceptor(private val apiConfig: ApiConfig) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val base = apiConfig.baseUrl.value.trim().trimEnd('/').toHttpUrlOrNull()
            ?: return chain.proceed(request) // invalid config — placeholder host fails loudly

        val rewritten = request.url.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .encodedPath(base.encodedPath.trimEnd('/') + request.url.encodedPath)
            .build()

        return chain.proceed(request.newBuilder().url(rewritten).build())
    }
}

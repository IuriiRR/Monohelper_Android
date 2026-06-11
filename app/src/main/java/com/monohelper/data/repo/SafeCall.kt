package com.monohelper.data.repo

import com.monohelper.core.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

/** Converts network/HTTP failures into [AppResult.Failure]; no exceptions cross layers. */
internal suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val detail = try {
        e.response()?.errorBody()?.string()?.take(300)
    } catch (_: IOException) {
        null
    }
    AppResult.Failure(
        if (detail.isNullOrBlank()) "Server error ${e.code()}" else "Server error ${e.code()}: $detail",
    )
} catch (e: IOException) {
    AppResult.Failure("Network error: ${e.message ?: "server unreachable"}")
} catch (e: Exception) {
    AppResult.Failure(e.message ?: "Unexpected error")
}

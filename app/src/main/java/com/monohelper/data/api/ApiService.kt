package com.monohelper.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the Monohelper Local backend.
 *
 * Paths are relative — the real base URL (which may carry a path prefix like
 * `/cloudapi`) is applied per request by [BaseUrlInterceptor].
 */
interface ApiService {

    @GET("accounts/")
    suspend fun listAccounts(@Query("user_id") userId: String? = null): AccountsResponseDto

    @GET("transactions/")
    suspend fun listTransactions(
        @Query("user_id") userId: String? = null,
        @Query("account_id") accountId: String? = null,
        @Query("limit") limit: Int? = null,
    ): TransactionsResponseDto

    @GET("reports/monthly")
    suspend fun monthlyReport(
        @Query("month") month: String,
        @Query("user_id") userId: String? = null,
    ): MonthlyReportResponseDto

    @POST("sync/accounts")
    suspend fun enqueueAccountsSync(@Body body: EmptyBodyDto = EmptyBodyDto()): EnqueueResponseDto

    @POST("sync/transactions")
    suspend fun enqueueTransactionsSync(
        @Body body: SyncTransactionsRequestDto = SyncTransactionsRequestDto(),
    ): EnqueueResponseDto

    @GET("tasks/{task_id}")
    suspend fun getTask(@Path("task_id") taskId: Long): TaskDto

    @GET("healthz")
    suspend fun healthz(): HealthzDto
}

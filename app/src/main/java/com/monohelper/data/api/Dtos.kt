package com.monohelper.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// One DTO per API.md model. DTOs never leave the data layer.

@Serializable
data class AccountDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String = "jar",
    @SerialName("send_id") val sendId: String? = null,
    @SerialName("currency_code") val currencyCode: Int = 980,
    val balance: Long = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    val title: String? = null,
    val goal: Long? = null,
    @SerialName("is_budget") val isBudget: Boolean = false,
    val invested: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("owner_username") val ownerUsername: String? = null,
)

@Serializable
data class AccountsResponseDto(val accounts: List<AccountDto> = emptyList())

@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("user_id") val userId: String,
    val time: Long,
    val description: String? = null,
    val amount: Long,
    @SerialName("operation_amount") val operationAmount: Long? = null,
    @SerialName("commission_rate") val commissionRate: Long? = null,
    @SerialName("cashback_amount") val cashbackAmount: Long? = null,
    val balance: Long,
    val hold: Boolean = false,
    val comment: String? = null,
    @SerialName("mcc_code") val mccCode: Int? = null,
    @SerialName("original_mcc") val originalMcc: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class TransactionsResponseDto(val transactions: List<TransactionDto> = emptyList())

@Serializable
data class TransactionPointDto(val time: Long, val balance: Long)

@Serializable
data class JarMonthlyReportDto(
    val id: String,
    val title: String? = null,
    @SerialName("current_balance") val currentBalance: Long,
    @SerialName("start_balance") val startBalance: Long,
    val budget: Long,
    @SerialName("total_deposits") val totalDeposits: Long,
    val spent: Long,
    val transactions: List<TransactionPointDto> = emptyList(),
)

@Serializable
data class MonthlyReportResponseDto(
    val month: String,
    val jars: List<JarMonthlyReportDto> = emptyList(),
)

@Serializable
data class TaskDto(
    val id: Long,
    val type: String,
    val payload: JsonObject? = null,
    val status: String,
    val result: JsonObject? = null,
    val error: String? = null,
    val attempts: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null,
)

@Serializable
data class EnqueueResponseDto(
    @SerialName("task_id") val taskId: Long,
    val status: String,
)

@Serializable
data class SyncTransactionsRequestDto(
    @SerialName("user_id") val userId: String? = null,
    val days: Int? = null,
)

/** `POST /sync/accounts` takes no body; FastAPI happily ignores an empty JSON object. */
@Serializable
class EmptyBodyDto

@Serializable
data class HealthzDto(
    val status: String,
    @SerialName("last_heartbeat_at") val lastHeartbeatAt: String? = null,
    @SerialName("last_error") val lastError: String? = null,
)

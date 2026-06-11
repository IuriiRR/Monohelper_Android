package com.monohelper.domain.model

// UI-facing models, mapped from DTOs in the data layer. No Android types here.

enum class AccountType { CARD, JAR, OTHER }

data class Account(
    val id: String,
    val userId: String,
    val type: AccountType,
    val currencyCode: Int,
    val balance: Long,
    val isActive: Boolean,
    val title: String?,
    val goal: Long?,
    val isBudget: Boolean,
    val invested: Long,
    val ownerUsername: String?,
)

data class Transaction(
    val id: String,
    val accountId: String,
    /** Unix seconds. */
    val time: Long,
    val description: String?,
    val amount: Long,
    val cashbackAmount: Long?,
    val balance: Long,
    val hold: Boolean,
    val comment: String?,
)

/** Query params for `GET /transactions/`; also the repository cache key. */
data class TransactionFilter(
    val userId: String? = null,
    val accountId: String? = null,
    val limit: Int = 100,
)

data class BalancePoint(
    /** Unix seconds. */
    val time: Long,
    val balance: Long,
)

data class JarReport(
    val id: String,
    val title: String?,
    val currentBalance: Long,
    val startBalance: Long,
    val budget: Long,
    val totalDeposits: Long,
    val spent: Long,
    val points: List<BalancePoint>,
)

data class MonthlyReport(
    /** `YYYY-MM`. */
    val month: String,
    val jars: List<JarReport>,
)

enum class TaskStatus {
    PENDING, RUNNING, SUCCESS, ERROR, UNKNOWN;

    val isTerminal: Boolean get() = this == SUCCESS || this == ERROR

    companion object {
        fun from(raw: String): TaskStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}

data class TaskInfo(
    val id: Long,
    val type: String,
    val status: TaskStatus,
    /** Human-readable flattening of the task `result` object. */
    val resultSummary: String?,
    val error: String?,
    val attempts: Int,
)

data class EnqueuedTask(
    val taskId: Long,
    val status: String,
)

data class WorkerHealth(
    val ok: Boolean,
    val lastHeartbeatAt: String?,
    val lastError: String?,
)

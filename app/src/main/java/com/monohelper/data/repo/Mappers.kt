package com.monohelper.data.repo

import com.monohelper.data.api.AccountDto
import com.monohelper.data.api.HealthzDto
import com.monohelper.data.api.JarMonthlyReportDto
import com.monohelper.data.api.MonthlyReportResponseDto
import com.monohelper.data.api.TaskDto
import com.monohelper.data.api.TransactionDto
import com.monohelper.data.api.TransactionPointDto
import com.monohelper.domain.model.Account
import com.monohelper.domain.model.AccountType
import com.monohelper.domain.model.BalancePoint
import com.monohelper.domain.model.JarReport
import com.monohelper.domain.model.MonthlyReport
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.model.Transaction
import com.monohelper.domain.model.WorkerHealth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// DTO → domain mapping happens here, inside the data layer.

internal fun AccountDto.toDomain() = Account(
    id = id,
    userId = userId,
    type = when (type.lowercase()) {
        "card" -> AccountType.CARD
        "jar" -> AccountType.JAR
        else -> AccountType.OTHER
    },
    currencyCode = currencyCode,
    balance = balance,
    isActive = isActive,
    title = title,
    goal = goal,
    isBudget = isBudget,
    invested = invested,
    ownerUsername = ownerUsername,
)

internal fun TransactionDto.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    time = time,
    description = description,
    amount = amount,
    cashbackAmount = cashbackAmount,
    balance = balance,
    hold = hold,
    comment = comment,
)

internal fun TransactionPointDto.toDomain() = BalancePoint(time = time, balance = balance)

internal fun JarMonthlyReportDto.toDomain() = JarReport(
    id = id,
    title = title,
    currentBalance = currentBalance,
    startBalance = startBalance,
    budget = budget,
    totalDeposits = totalDeposits,
    spent = spent,
    points = transactions.map { it.toDomain() },
)

internal fun MonthlyReportResponseDto.toDomain() = MonthlyReport(
    month = month,
    jars = jars.map { it.toDomain() },
)

internal fun TaskDto.toDomain() = TaskInfo(
    id = id,
    type = type,
    status = TaskStatus.from(status),
    resultSummary = result?.toSummary(),
    error = error,
    attempts = attempts,
)

internal fun HealthzDto.toDomain() = WorkerHealth(
    ok = status == "ok" && lastError == null,
    lastHeartbeatAt = lastHeartbeatAt,
    lastError = lastError,
)

/** Flattens a task `result` object into readable `key: value` lines for the UI. */
internal fun JsonObject.toSummary(): String =
    entries.joinToString("\n") { (key, value) -> "$key: ${value.display()}" }

private fun JsonElement.display(): String = when (this) {
    is JsonPrimitive -> content
    is JsonArray -> if (isEmpty()) "none" else joinToString("; ") { it.display() }
    is JsonObject -> entries.joinToString(", ") { (k, v) -> "$k: ${v.display()}" }
}

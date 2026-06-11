package com.monohelper.testutil

import com.monohelper.domain.model.Account
import com.monohelper.domain.model.AccountType
import com.monohelper.domain.model.BalancePoint
import com.monohelper.domain.model.JarReport
import com.monohelper.domain.model.MonthlyReport
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.model.Transaction

/** Synthetic domain-model fixtures shared across tests. */
object TestData {

    fun account(
        id: String = "acc-1",
        type: AccountType = AccountType.JAR,
        balance: Long = 150_000,
        title: String? = "Vacation",
        goal: Long? = 500_000,
        isBudget: Boolean = false,
        ownerUsername: String? = "tester",
    ) = Account(
        id = id,
        userId = "user-1",
        type = type,
        currencyCode = 980,
        balance = balance,
        isActive = true,
        title = title,
        goal = goal,
        isBudget = isBudget,
        invested = 0,
        ownerUsername = ownerUsername,
    )

    fun transaction(
        id: String = "tx-1",
        accountId: String = "acc-1",
        time: Long = 1_704_067_200,
        amount: Long = -15_000,
        balance: Long = 135_000,
        description: String? = "Grocery store",
    ) = Transaction(
        id = id,
        accountId = accountId,
        time = time,
        description = description,
        amount = amount,
        cashbackAmount = 150,
        balance = balance,
        hold = false,
        comment = null,
    )

    fun jarReport(
        id: String = "jar-1",
        title: String? = "Groceries",
        points: List<BalancePoint> = listOf(
            BalancePoint(time = 1_704_067_200, balance = 500_000),
            BalancePoint(time = 1_704_153_600, balance = 450_000),
        ),
    ) = JarReport(
        id = id,
        title = title,
        currentBalance = 450_000,
        startBalance = 0,
        budget = 500_000,
        totalDeposits = 500_000,
        spent = -50_000,
        points = points,
    )

    fun monthlyReport(
        month: String = "2024-01",
        jars: List<JarReport> = listOf(jarReport()),
    ) = MonthlyReport(month = month, jars = jars)

    fun taskInfo(
        id: Long = 1,
        type: String = "sync_accounts",
        status: TaskStatus = TaskStatus.SUCCESS,
        resultSummary: String? = "status: success",
        error: String? = null,
    ) = TaskInfo(
        id = id,
        type = type,
        status = status,
        resultSummary = resultSummary,
        error = error,
        attempts = 1,
    )
}

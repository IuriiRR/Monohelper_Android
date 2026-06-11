package com.monohelper.ui.nav

import android.net.Uri

object Routes {
    const val DASHBOARD = "dashboard"
    const val ACCOUNTS = "accounts"
    const val TRANSACTIONS = "transactions"
    const val SYNC = "sync"

    const val ARG_ACCOUNT_ID = "accountId"
    const val TRANSACTIONS_PATTERN = "$TRANSACTIONS?$ARG_ACCOUNT_ID={$ARG_ACCOUNT_ID}"

    /** Transactions route, optionally pre-filtered to one account. */
    fun transactions(accountId: String? = null): String =
        if (accountId == null) TRANSACTIONS
        else "$TRANSACTIONS?$ARG_ACCOUNT_ID=${Uri.encode(accountId)}"
}

package com.monohelper.ui.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monohelper.core.format.DateFmt
import com.monohelper.domain.model.Transaction
import com.monohelper.ui.components.ErrorRetry
import com.monohelper.ui.components.LoadingBox
import com.monohelper.ui.components.MoneyText

@Composable
fun TransactionsRoute(viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TransactionsScreen(
        state = state,
        accountFiltered = viewModel.accountId != null,
        onRetry = viewModel::refresh,
    )
}

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    accountFiltered: Boolean,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (accountFiltered) {
            Text(
                text = "Filtered by account",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        when (state) {
            is TransactionsUiState.Loading -> LoadingBox()
            is TransactionsUiState.Error -> ErrorRetry(message = state.message, onRetry = onRetry)
            is TransactionsUiState.Success -> {
                if (state.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No transactions")
                    }
                } else {
                    // API already returns newest first — keep order.
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.transactions, key = { it.id }) { transaction ->
                            TransactionRow(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description ?: "(no description)",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = DateFmt.format(transaction.time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            MoneyText(
                amountMinor = transaction.amount,
                colorBySign = true,
                signed = true,
            )
            MoneyText(
                amountMinor = transaction.balance,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            if (transaction.hold) {
                Text(
                    text = "HOLD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

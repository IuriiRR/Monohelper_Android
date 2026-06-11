package com.monohelper.ui.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monohelper.domain.model.Account
import com.monohelper.domain.model.AccountType
import com.monohelper.ui.components.ErrorRetry
import com.monohelper.ui.components.LoadingBox
import com.monohelper.ui.components.MoneyText

@Composable
fun AccountsRoute(
    onAccountClick: (String) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountsScreen(
        state = state,
        onAccountClick = onAccountClick,
        onRetry = viewModel::refresh,
    )
}

@Composable
fun AccountsScreen(
    state: AccountsUiState,
    onAccountClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is AccountsUiState.Loading -> LoadingBox()
        is AccountsUiState.Error -> ErrorRetry(message = state.message, onRetry = onRetry)
        is AccountsUiState.Success -> {
            if (state.accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No accounts yet — run a sync")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.accounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            onClick = { onAccountClick(account.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (account.isActive) 1f else 0.5f)
                .padding(16.dp),
        ) {
            val title = account.title
                ?: if (account.type == AccountType.CARD) "Card" else "Account"
            val owner = account.ownerUsername ?: account.userId
            val secondary = buildString {
                append(account.type.name.lowercase())
                append(" · ")
                append(owner)
                if (!account.isActive) append(" (inactive)")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                MoneyText(
                    amountMinor = account.balance,
                    currencyCode = account.currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val goal = account.goal
            if (account.type == AccountType.JAR && goal != null && goal > 0) {
                LinearProgressIndicator(
                    progress = { (account.balance.toFloat() / goal).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Goal: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    MoneyText(
                        amountMinor = goal,
                        currencyCode = account.currencyCode,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

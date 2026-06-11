package com.monohelper.ui.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monohelper.core.format.DateFmt
import com.monohelper.domain.model.JarReport
import com.monohelper.ui.components.BalanceLineChart
import com.monohelper.ui.components.ErrorRetry
import com.monohelper.ui.components.LoadingBox
import com.monohelper.ui.components.MoneyText
import java.time.YearMonth

@Composable
fun DashboardRoute(viewModel: DashboardViewModel = hiltViewModel()) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardScreen(
        month = month,
        state = state,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onRetry = viewModel::refresh,
    )
}

@Composable
fun DashboardScreen(
    month: YearMonth,
    state: DashboardUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = DateFmt.monthLabel(month.toString()),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = onNextMonth,
                enabled = month < YearMonth.now(),
            ) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }
        when (state) {
            is DashboardUiState.Loading -> LoadingBox()
            is DashboardUiState.Error -> ErrorRetry(message = state.message, onRetry = onRetry)
            is DashboardUiState.Success -> {
                if (state.report.jars.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No budget jars for this month")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.report.jars, key = { it.id }) { jar ->
                            JarCard(jar = jar)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JarCard(jar: JarReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = jar.title ?: "Untitled jar",
                style = MaterialTheme.typography.titleMedium,
            )
            MoneyText(
                amountMinor = jar.currentBalance,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            LabelValueRow(label = "Start balance") {
                MoneyText(amountMinor = jar.startBalance)
            }
            LabelValueRow(label = "Budget") {
                MoneyText(amountMinor = jar.budget)
            }
            LabelValueRow(label = "Deposits") {
                MoneyText(amountMinor = jar.totalDeposits)
            }
            LabelValueRow(label = "Spent") {
                MoneyText(amountMinor = jar.spent, colorBySign = true)
            }
            BalanceLineChart(
                points = jar.points,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun LabelValueRow(
    label: String,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        value()
    }
}

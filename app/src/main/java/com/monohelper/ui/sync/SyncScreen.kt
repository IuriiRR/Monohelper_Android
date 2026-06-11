package com.monohelper.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monohelper.domain.model.TaskInfo
import com.monohelper.domain.model.TaskStatus
import com.monohelper.domain.usecase.SyncKind
import com.monohelper.ui.components.IncomeGreen

@Composable
fun SyncRoute(viewModel: SyncViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SyncScreen(
        state = state,
        onSyncAccounts = { viewModel.start(SyncKind.ACCOUNTS) },
        onSyncTransactions = { viewModel.start(SyncKind.TRANSACTIONS) },
        onReset = viewModel::reset,
    )
}

@Composable
fun SyncScreen(
    state: SyncRunState,
    onSyncAccounts: () -> Unit,
    onSyncTransactions: () -> Unit,
    onReset: () -> Unit,
) {
    val buttonsEnabled = state is SyncRunState.Idle ||
        state is SyncRunState.Finished ||
        state is SyncRunState.Failed

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onSyncAccounts, enabled = buttonsEnabled) {
                Text("Sync accounts")
            }
            Button(onClick = onSyncTransactions, enabled = buttonsEnabled) {
                Text("Sync transactions")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (state) {
                    SyncRunState.Idle -> Text("Pick a sync to run")
                    is SyncRunState.Enqueueing -> ProgressLine("Enqueueing…")
                    is SyncRunState.Polling -> ProgressLine(pollingLabel(state.task))
                    is SyncRunState.Finished -> FinishedContent(task = state.task, onReset = onReset)
                    is SyncRunState.Failed -> {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onReset) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

private fun pollingLabel(task: TaskInfo?): String =
    if (task == null) "Waiting for task…" else "Task #${task.id} — ${task.status.name.lowercase()}"

@Composable
private fun ProgressLine(label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
        Text(label)
    }
}

@Composable
private fun FinishedContent(task: TaskInfo, onReset: () -> Unit) {
    if (task.status == TaskStatus.SUCCESS) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Success", tint = IncomeGreen)
    } else {
        Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
    }
    val detail = task.resultSummary ?: task.error
    if (detail != null) {
        Text(detail)
    }
    TextButton(onClick = onReset) {
        Text("Done")
    }
}

package com.monohelper.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monohelper.core.result.AppResult
import com.monohelper.ui.nav.HealthViewModel

private val WarningAmber = Color(0xFFF9A825)

/** Small worker-status dot in the app bar, fed by `GET /healthz`. */
@Composable
fun HealthBadge(viewModel: HealthViewModel = hiltViewModel()) {
    val health by viewModel.health.collectAsStateWithLifecycle()

    val outline = MaterialTheme.colorScheme.outline
    val error = MaterialTheme.colorScheme.error
    val (color, description) = when (val current = health) {
        null -> outline to "Worker status unknown"
        is AppResult.Failure -> error to "Backend unreachable"
        is AppResult.Success ->
            if (current.value.ok) {
                IncomeGreen to "Worker healthy"
            } else {
                WarningAmber to "Worker error: ${current.value.lastError ?: "unknown"}"
            }
    }

    Icon(
        imageVector = Icons.Filled.Circle,
        contentDescription = description,
        tint = color,
        modifier = Modifier.padding(horizontal = 8.dp).size(12.dp),
    )
}

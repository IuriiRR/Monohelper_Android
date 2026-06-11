package com.monohelper.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.monohelper.core.format.Money

/** Green used for income amounts (Material has no built-in "positive" color). */
val IncomeGreen = Color(0xFF2E7D32)

/**
 * The only way money is rendered in the app — delegates to [Money.format].
 */
@Composable
fun MoneyText(
    amountMinor: Long,
    modifier: Modifier = Modifier,
    currencyCode: Int = Money.UAH,
    colorBySign: Boolean = false,
    signed: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
) {
    val text = if (signed) Money.formatSigned(amountMinor, currencyCode) else Money.format(amountMinor, currencyCode)
    val color = when {
        !colorBySign -> Color.Unspecified
        amountMinor < 0 -> MaterialTheme.colorScheme.error
        amountMinor > 0 -> IncomeGreen
        else -> Color.Unspecified
    }
    Text(text = text, color = color, style = style, fontWeight = fontWeight, modifier = modifier)
}

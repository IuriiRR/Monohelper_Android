package com.monohelper.core.format

import java.util.Locale

/**
 * Single money formatter for the whole app.
 *
 * All API monetary values are integers in minor currency units
 * (e.g. kopecks; `15000` = ₴150.00). No inline `/100` math anywhere else.
 */
object Money {

    const val UAH = 980

    private val symbols = mapOf(
        980 to "₴",
        840 to "$",
        978 to "€",
        985 to "zł",
    )

    /** `-15000, 980` → `"-₴150.00"`. Unknown currency falls back to the numeric code. */
    fun format(minor: Long, currencyCode: Int = UAH): String {
        val negative = minor < 0
        val abs = if (minor == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(minor)
        val symbol = symbols[currencyCode] ?: "¤$currencyCode "
        val major = String.format(Locale.US, "%,d", abs / 100)
        val cents = String.format(Locale.US, "%02d", abs % 100)
        return buildString {
            if (negative) append('-')
            append(symbol)
            append(major)
            append('.')
            append(cents)
        }
    }

    /** Like [format] but prefixes positive amounts with `+` (for transaction rows). */
    fun formatSigned(minor: Long, currencyCode: Int = UAH): String =
        if (minor > 0) "+${format(minor, currencyCode)}" else format(minor, currencyCode)
}

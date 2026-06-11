package com.monohelper.core.format

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Single date/time formatter for the whole app.
 * API `time` fields are Unix timestamps in **seconds**.
 */
object DateFmt {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH)
    private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.ENGLISH)

    /** `1704067200` → `"01.01.2024 02:00"` (in [zone]). */
    fun format(unixSeconds: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochSecond(unixSeconds).atZone(zone).format(dateTimeFormatter)

    /** `1704067200` → `"01.01.2024"` (in [zone]). */
    fun formatDate(unixSeconds: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochSecond(unixSeconds).atZone(zone).format(dateFormatter)

    /** `"2024-01"` → `"January 2024"`. Invalid input is returned unchanged. */
    fun monthLabel(month: String): String = try {
        YearMonth.parse(month).format(monthFormatter)
    } catch (_: java.time.format.DateTimeParseException) {
        month
    }
}

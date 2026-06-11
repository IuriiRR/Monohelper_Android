package com.monohelper.core.format

import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DateFmtTest {

    @Test
    fun `format renders date and time in utc`() {
        assertEquals("01.01.2024 00:00", DateFmt.format(1704067200, ZoneOffset.UTC))
    }

    @Test
    fun `formatDate renders date only in utc`() {
        assertEquals("01.01.2024", DateFmt.formatDate(1704067200, ZoneOffset.UTC))
    }

    @Test
    fun `monthLabel renders english month name and year`() {
        assertEquals("January 2024", DateFmt.monthLabel("2024-01"))
    }

    @Test
    fun `monthLabel returns invalid input unchanged`() {
        assertEquals("garbage", DateFmt.monthLabel("garbage"))
    }
}

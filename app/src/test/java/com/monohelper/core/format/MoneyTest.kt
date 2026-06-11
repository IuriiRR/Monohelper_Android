package com.monohelper.core.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats positive amount in default currency`() {
        assertEquals("₴150.00", Money.format(15000))
    }

    @Test
    fun `formats negative amount with leading minus`() {
        assertEquals("-₴150.00", Money.format(-15000))
    }

    @Test
    fun `formats zero`() {
        assertEquals("₴0.00", Money.format(0))
    }

    @Test
    fun `groups thousands with commas`() {
        assertEquals("₴12,345.67", Money.format(1234567))
    }

    @Test
    fun `formats usd with dollar symbol`() {
        assertEquals("$150.00", Money.format(15000, 840))
    }

    @Test
    fun `unknown currency falls back to numeric code`() {
        assertTrue(Money.format(100, 999).startsWith("¤999"))
    }

    @Test
    fun `formatSigned prefixes positive amounts with plus`() {
        assertEquals("+₴1.50", Money.formatSigned(150))
    }

    @Test
    fun `formatSigned keeps minus for negative amounts`() {
        assertEquals("-₴1.50", Money.formatSigned(-150))
    }

    @Test
    fun `pads cents below ten`() {
        assertEquals("₴0.05", Money.format(5))
    }
}

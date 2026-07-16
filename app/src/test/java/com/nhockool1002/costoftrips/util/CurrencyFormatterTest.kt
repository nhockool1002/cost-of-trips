package com.nhockool1002.costoftrips.util

import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `VND has no decimals and symbol after the amount`() {
        val formatted = CurrencyFormatter.format(1_000_000.0, AppCurrency.VND)
        assertTrue(formatted.endsWith(AppCurrency.VND.symbol))
        assertTrue(formatted.contains("1.000.000") || formatted.contains("1,000,000"))
    }

    @Test
    fun `USD has two decimals and symbol before the amount`() {
        val formatted = CurrencyFormatter.format(1234.5, AppCurrency.USD)
        assertTrue(formatted.startsWith(AppCurrency.USD.symbol))
        assertTrue(formatted.contains("1,234.50"))
    }

    @Test
    fun `JPY has no decimals`() {
        val formatted = CurrencyFormatter.format(500.0, AppCurrency.JPY)
        assertTrue(formatted.startsWith(AppCurrency.JPY.symbol))
        assertTrue(!formatted.contains("."))
    }

    @Test
    fun `zero amount formats without throwing`() {
        val formatted = CurrencyFormatter.format(0.0, AppCurrency.EUR)
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun `default currency parameter is VND`() {
        assertEquals(CurrencyFormatter.format(100.0, AppCurrency.VND), CurrencyFormatter.format(100.0))
    }
}

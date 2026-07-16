package com.nhockool1002.costoftrips.util

import androidx.compose.ui.text.AnnotatedString
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

    @Test
    fun `rawDigitsToAmount treats the trailing decimalDigits digits as the decimal part`() {
        assertEquals(1234.56, rawDigitsToAmount("123456", AppCurrency.USD), 0.0001)
        assertEquals(123456.0, rawDigitsToAmount("123456", AppCurrency.VND), 0.0001)
    }

    @Test
    fun `rawDigitsToAmount handles short input as sub-unit digits`() {
        // "5" typed for a 2-decimal currency is 0.05, matching a calculator-style amount field.
        assertEquals(0.05, rawDigitsToAmount("5", AppCurrency.USD), 0.0001)
    }

    @Test
    fun `rawDigitsToAmount treats blank or non-numeric input as zero`() {
        assertEquals(0.0, rawDigitsToAmount("", AppCurrency.USD), 0.0001)
    }

    @Test
    fun `amountToRawDigits is the inverse of rawDigitsToAmount`() {
        assertEquals("123456", amountToRawDigits(1234.56, AppCurrency.USD))
        assertEquals("123456", amountToRawDigits(123456.0, AppCurrency.VND))
    }

    @Test
    fun `amountToRawDigits returns empty for a zero or negative amount`() {
        assertEquals("", amountToRawDigits(0.0, AppCurrency.USD))
        assertEquals("", amountToRawDigits(-5.0, AppCurrency.USD))
    }

    @Test
    fun `formatRawDigits groups thousands and applies the currency's decimal digits`() {
        assertEquals("1,234.56", formatRawDigits("123456", AppCurrency.USD))
    }

    @Test
    fun `formatRawDigits returns empty for empty input`() {
        assertEquals("", formatRawDigits("", AppCurrency.USD))
    }

    @Test
    fun `CurrencyAmountVisualTransformation renders the formatted amount and pins the cursor to the end`() {
        val transformation = CurrencyAmountVisualTransformation(AppCurrency.USD)
        val result = transformation.filter(AnnotatedString("123456"))

        assertEquals("1,234.56", result.text.text)
        assertEquals(result.text.length, result.offsetMapping.originalToTransformed(3))
        assertEquals(6, result.offsetMapping.transformedToOriginal(2))
    }
}

package com.nhockool1002.costoftrips.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCurrencyTest {

    @Test
    fun `fromCode resolves a known code`() {
        assertEquals(AppCurrency.USD, AppCurrency.fromCode("USD"))
    }

    @Test
    fun `fromCode falls back to VND for an unknown code`() {
        assertEquals(AppCurrency.VND, AppCurrency.fromCode("XYZ"))
    }

    @Test
    fun `fromCode falls back to VND for null`() {
        assertEquals(AppCurrency.VND, AppCurrency.fromCode(null))
    }

    @Test
    fun `every currency code round-trips through fromCode`() {
        AppCurrency.entries.forEach { currency ->
            assertEquals(currency, AppCurrency.fromCode(currency.code))
        }
    }
}

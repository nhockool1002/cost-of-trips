package com.nhockool1002.costoftrips.util

import androidx.compose.runtime.compositionLocalOf
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import java.text.NumberFormat

val LocalCurrency = compositionLocalOf { AppCurrency.VND }

object CurrencyFormatter {
    fun format(amount: Double, currency: AppCurrency = AppCurrency.VND): String {
        val formatter = NumberFormat.getNumberInstance(currency.groupingLocale).apply {
            minimumFractionDigits = currency.decimalDigits
            maximumFractionDigits = currency.decimalDigits
        }
        val number = formatter.format(amount)
        return if (currency.symbolBefore) "${currency.symbol}$number" else "$number ${currency.symbol}"
    }
}

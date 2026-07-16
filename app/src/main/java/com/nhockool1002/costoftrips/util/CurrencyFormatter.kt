package com.nhockool1002.costoftrips.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import java.text.NumberFormat
import kotlin.math.pow
import kotlin.math.roundToLong

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

/**
 * Converts a raw digits-only amount string (e.g. "123456", as typed into a currency
 * amount field) into its actual value, treating the trailing [AppCurrency.decimalDigits]
 * digits as the decimal part - e.g. "123456" is 1234.56 for a 2-decimal currency, or
 * 123456 for a 0-decimal one. This is the "type digits, decimal point shifts in" input
 * style used by most POS/banking apps, which sidesteps locale decimal-separator
 * ambiguity entirely.
 */
fun rawDigitsToAmount(rawDigits: String, currency: AppCurrency): Double {
    val value = rawDigits.toLongOrNull() ?: return 0.0
    return value / 10.0.pow(currency.decimalDigits)
}

/** The inverse of [rawDigitsToAmount], used to pre-fill a field from a stored amount. */
fun amountToRawDigits(amount: Double, currency: AppCurrency): String {
    val scaled = (amount * 10.0.pow(currency.decimalDigits)).roundToLong()
    return if (scaled <= 0) "" else scaled.toString()
}

/** Live-formats [rawDigits] the same way [CurrencyFormatter.format] would, minus the currency symbol. */
fun formatRawDigits(rawDigits: String, currency: AppCurrency): String {
    if (rawDigits.isEmpty()) return ""
    val formatter = NumberFormat.getNumberInstance(currency.groupingLocale).apply {
        minimumFractionDigits = currency.decimalDigits
        maximumFractionDigits = currency.decimalDigits
    }
    return formatter.format(rawDigitsToAmount(rawDigits, currency))
}

/**
 * Shows [formatRawDigits]'s output in place of the raw digits the user is actually typing,
 * so a currency field displays "1,234.56" while typing "123456". The cursor is always
 * pinned to the end, since the formatted text's digit positions don't map 1:1 to the raw
 * input once grouping separators are inserted.
 */
class CurrencyAmountVisualTransformation(private val currency: AppCurrency) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = formatRawDigits(text.text, currency)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = formatted.length
            override fun transformedToOriginal(offset: Int) = text.text.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

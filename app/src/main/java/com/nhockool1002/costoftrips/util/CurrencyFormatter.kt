package com.nhockool1002.costoftrips.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import java.text.DecimalFormatSymbols
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

    /** Renders a raw amount for an editable input field: no scientific notation, no trailing ".0". */
    fun toInputString(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}

/**
 * Strips an amount text field's input down to digits plus, for currencies that carry a fractional
 * part, a single decimal point followed by at most [AppCurrency.decimalDigits] digits. The decimal
 * point itself is always typed as '.', regardless of the currency's grouping locale — matching the
 * '.' key every numeric keyboard shows, and keeping the raw stored string trivially parseable with
 * String.toDoubleOrNull().
 */
fun sanitizeAmountInput(input: String, currency: AppCurrency): String {
    val allowDecimal = currency.decimalDigits > 0
    val sb = StringBuilder()
    var seenDot = false
    for (c in input) {
        when {
            c.isDigit() -> sb.append(c)
            c == '.' && allowDecimal && !seenDot -> {
                sb.append(c)
                seenDot = true
            }
        }
    }
    val dotIndex = sb.indexOf(".")
    if (dotIndex == -1) return sb.toString()
    val decimals = sb.substring(dotIndex + 1).take(currency.decimalDigits)
    return sb.substring(0, dotIndex) + "." + decimals
}

/**
 * Groups the integer part of an amount field's raw digit string as the user types, using the
 * separator the selected currency's locale groups with (e.g. "1,234,567" for USD, "1.234.567" for
 * VND) — the same grouping [CurrencyFormatter.format] applies when rendering saved totals, so what
 * you type looks like what you'll see afterwards. The underlying field value stays the plain,
 * ungrouped digit string; only the on-screen rendering changes.
 */
class CurrencyGroupingVisualTransformation(private val currency: AppCurrency) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val dotIndex = raw.indexOf('.')
        val integerPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
        val fractionPart = if (dotIndex >= 0) raw.substring(dotIndex) else ""
        val groupingChar = DecimalFormatSymbols.getInstance(currency.groupingLocale).groupingSeparator

        val n = integerPart.length
        val grouped = StringBuilder()
        val originalToTransformedInt = IntArray(n + 1)
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) {
                grouped.append(groupingChar)
            }
            originalToTransformedInt[i] = grouped.length
            grouped.append(integerPart[i])
        }
        originalToTransformedInt[n] = grouped.length

        val transformed = grouped.toString() + fractionPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= n) originalToTransformedInt[offset] else grouped.length + (offset - n)

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= grouped.length) {
                    grouped.take(offset).count { it != groupingChar }
                } else {
                    n + (offset - grouped.length)
                }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}

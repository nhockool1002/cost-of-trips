package com.nhockool1002.costoftrips.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    fun format(amount: Double): String = "${formatter.format(amount)} ₫"
}

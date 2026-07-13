package com.nhockool1002.costoftrips.data.preferences

import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val decimalDigits: Int,
    val symbolBefore: Boolean,
    val groupingLocale: Locale
) {
    VND("VND", "₫", 0, false, Locale("vi", "VN")),
    USD("USD", "$", 2, true, Locale.US),
    EUR("EUR", "€", 2, true, Locale.GERMANY),
    GBP("GBP", "£", 2, true, Locale.UK),
    JPY("JPY", "¥", 0, true, Locale.JAPAN),
    KRW("KRW", "₩", 0, true, Locale.KOREA),
    THB("THB", "฿", 2, true, Locale("th", "TH"));

    companion object {
        fun fromCode(code: String?): AppCurrency = entries.find { it.code == code } ?: VND
    }
}

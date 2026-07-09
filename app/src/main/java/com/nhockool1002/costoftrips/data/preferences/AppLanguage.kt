package com.nhockool1002.costoftrips.data.preferences

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    VIETNAMESE("vi");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.find { it.code == code } ?: ENGLISH
    }
}

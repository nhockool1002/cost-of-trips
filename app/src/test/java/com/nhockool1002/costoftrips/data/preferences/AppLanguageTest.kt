package com.nhockool1002.costoftrips.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `fromCode resolves Vietnamese`() {
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.fromCode("vi"))
    }

    @Test
    fun `fromCode falls back to English for an unknown code`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("fr"))
    }

    @Test
    fun `fromCode falls back to English for null`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(null))
    }

    @Test
    fun `every language code round-trips through fromCode`() {
        AppLanguage.entries.forEach { language ->
            assertEquals(language, AppLanguage.fromCode(language.code))
        }
    }
}

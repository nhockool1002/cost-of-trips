package com.nhockool1002.costoftrips.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY = stringPreferencesKey("currency")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val RATE_DIALOG_LAST_SHOWN_AT = longPreferencesKey("rate_dialog_last_shown_at")
        val RATE_DIALOG_DISMISSED_PERMANENTLY = booleanPreferencesKey("rate_dialog_dismissed_permanently")
    }

    val themeMode = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.DARK
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val currency = context.dataStore.data.map { prefs ->
        AppCurrency.fromCode(prefs[Keys.CURRENCY])
    }

    suspend fun setCurrency(currency: AppCurrency) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency.code }
    }

    val reminderEnabled = context.dataStore.data.map { prefs -> prefs[Keys.REMINDER_ENABLED] ?: false }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    val reminderIntervalHours = context.dataStore.data.map { prefs -> prefs[Keys.REMINDER_INTERVAL_HOURS] ?: 6 }

    suspend fun setReminderIntervalHours(hours: Int) {
        context.dataStore.edit { it[Keys.REMINDER_INTERVAL_HOURS] = hours }
    }

    val rateDialogLastShownAt = context.dataStore.data.map { prefs -> prefs[Keys.RATE_DIALOG_LAST_SHOWN_AT] ?: 0L }

    suspend fun setRateDialogLastShownAt(timestampMillis: Long) {
        context.dataStore.edit { it[Keys.RATE_DIALOG_LAST_SHOWN_AT] = timestampMillis }
    }

    val rateDialogDismissedPermanently = context.dataStore.data.map { prefs ->
        prefs[Keys.RATE_DIALOG_DISMISSED_PERMANENTLY] ?: false
    }

    suspend fun setRateDialogDismissedPermanently(dismissed: Boolean) {
        context.dataStore.edit { it[Keys.RATE_DIALOG_DISMISSED_PERMANENTLY] = dismissed }
    }
}

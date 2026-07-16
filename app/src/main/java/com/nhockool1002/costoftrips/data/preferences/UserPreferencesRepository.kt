package com.nhockool1002.costoftrips.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { LIGHT, DARK, SYSTEM }

// Takes a DataStore directly (rather than building one from a Context internally) so tests can
// supply an isolated, freshly-created instance instead of going through the Context.dataStore
// delegate above, which is a process-wide singleton keyed by file path - sharing it across test
// classes in the same JVM caused cross-test interference (in-flight writes from one test class's
// leftover coroutines racing a later class's reads/writes) that showed up as sporadic
// UncompletedCoroutinesError hangs.
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.dataStore)

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY = stringPreferencesKey("currency")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val RATE_DIALOG_LAST_SHOWN_AT = longPreferencesKey("rate_dialog_last_shown_at")
        val RATE_DIALOG_DISMISSED_PERMANENTLY = booleanPreferencesKey("rate_dialog_dismissed_permanently")
    }

    val themeMode = dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.DARK
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val currency = dataStore.data.map { prefs ->
        AppCurrency.fromCode(prefs[Keys.CURRENCY])
    }

    suspend fun setCurrency(currency: AppCurrency) {
        dataStore.edit { it[Keys.CURRENCY] = currency.code }
    }

    val reminderEnabled = dataStore.data.map { prefs -> prefs[Keys.REMINDER_ENABLED] ?: false }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    val reminderIntervalHours = dataStore.data.map { prefs -> prefs[Keys.REMINDER_INTERVAL_HOURS] ?: 6 }

    suspend fun setReminderIntervalHours(hours: Int) {
        dataStore.edit { it[Keys.REMINDER_INTERVAL_HOURS] = hours }
    }

    val rateDialogLastShownAt = dataStore.data.map { prefs -> prefs[Keys.RATE_DIALOG_LAST_SHOWN_AT] ?: 0L }

    suspend fun setRateDialogLastShownAt(timestampMillis: Long) {
        dataStore.edit { it[Keys.RATE_DIALOG_LAST_SHOWN_AT] = timestampMillis }
    }

    val rateDialogDismissedPermanently = dataStore.data.map { prefs ->
        prefs[Keys.RATE_DIALOG_DISMISSED_PERMANENTLY] ?: false
    }

    suspend fun setRateDialogDismissedPermanently(dismissed: Boolean) {
        dataStore.edit { it[Keys.RATE_DIALOG_DISMISSED_PERMANENTLY] = dismissed }
    }
}

package com.nhockool1002.costoftrips.testutil

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import java.util.UUID

/**
 * Builds a fresh [DataStore] backed by its own uniquely-named temp file, instead of going
 * through the `Context.dataStore by preferencesDataStore(name = "settings")` delegate, which is
 * a process-wide singleton keyed by file path - every test using that delegate shares the exact
 * same DataStore instance for the whole Gradle test JVM, including its background writer
 * coroutines that never get torn down between tests. That caused cross-test-class interference
 * (in-flight writes from one test class racing a later class's reads) which surfaced as sporadic
 * UncompletedCoroutinesError hangs.
 */
object InMemoryPreferencesDataStoreFactory {
    fun create(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File.createTempFile("test-prefs-${UUID.randomUUID()}", ".preferences_pb") }
    )
}

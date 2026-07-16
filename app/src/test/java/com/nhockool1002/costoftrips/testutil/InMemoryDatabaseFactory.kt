package com.nhockool1002.costoftrips.testutil

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nhockool1002.costoftrips.data.local.AppDatabase

/** Builds a fresh in-memory Room database (Robolectric context) for repository/DAO tests. */
object InMemoryDatabaseFactory {
    fun create(): AppDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
}

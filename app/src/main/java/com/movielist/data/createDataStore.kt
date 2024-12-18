package com.movielist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_FILE_NAME)

fun createDataStore(context: Context): DataStore<Preferences> {
    return context.dataStore
}





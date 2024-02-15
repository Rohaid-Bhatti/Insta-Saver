package com.hazel.instadownloader.app.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStores {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
    private val PERMISSION_REQUEST_COUNT_KEY = intPreferencesKey("PERMISSION_REQUEST_COUNT_KEY")

    // Function to store the permission request count
    suspend fun storePermissionRequestCount(count: Int, context: Context) {
        context.dataStore.edit { settings ->
            settings[PERMISSION_REQUEST_COUNT_KEY] = count
        }
    }

    // Function to get the permission request count
    fun getPermissionRequestCount(context: Context): Flow<Int> {
        return context.dataStore.data.map { settings ->
            settings[PERMISSION_REQUEST_COUNT_KEY] ?: 0
        }
    }
}
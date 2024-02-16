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
    private val BOTTOM_SHEET_SHOWN_KEY = booleanPreferencesKey("BOTTOM_SHEET_SHOWN_KEY")

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

    // Function to store whether the bottom sheet has been shown
    suspend fun storeBottomSheetShown(context: Context, shown: Boolean) {
        context.dataStore.edit { settings ->
            settings[BOTTOM_SHEET_SHOWN_KEY] = shown
        }
    }

    // Function to check if the bottom sheet has been shown
    fun isBottomSheetShown(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { settings ->
            settings[BOTTOM_SHEET_SHOWN_KEY] ?: false
        }
    }
}
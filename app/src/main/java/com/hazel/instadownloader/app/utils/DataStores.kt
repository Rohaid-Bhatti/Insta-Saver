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
    private val LANGUAGE_SELECTED_KEY = booleanPreferencesKey("LANGUAGE_SELECTED_KEY")
//    private val SAVE_BUTTON_SHOWN_KEY = booleanPreferencesKey("SAVE_BUTTON_SHOWN_KEY")

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

    // Function to store whether the language activity has been shown
    suspend fun storeLanguageSelected(context: Context, selected: Boolean) {
        context.dataStore.edit { settings ->
            settings[LANGUAGE_SELECTED_KEY] = selected
        }
    }

    // Function to check if the language activity has been shown
    fun isLanguageSelected(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { settings ->
            settings[LANGUAGE_SELECTED_KEY] ?: false
        }
    }

    /*suspend fun storeSaveButtonShown(context: Context, shown: Boolean) {
        context.dataStore.edit { settings ->
            settings[SAVE_BUTTON_SHOWN_KEY] = shown
        }
    }

    fun isSaveButtonShown(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { settings ->
            settings[SAVE_BUTTON_SHOWN_KEY] ?: false
        }
    }*/
}
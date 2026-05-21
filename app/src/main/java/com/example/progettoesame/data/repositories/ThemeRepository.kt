package com.example.progettoesame.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.progettoesame.ui.utils.Theme
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamicColor")
    }

    val theme = dataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[THEME_KEY] ?: "System")
        } catch (_: Exception) {
            Theme.Light
        }
    }

    val dynamicColor = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: false
    }

    suspend fun setTheme(theme: Theme) = dataStore.edit { preferences ->
        preferences[THEME_KEY] = theme.toString()
    }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { preferences ->
        preferences[DYNAMIC_COLOR_KEY] = enabled
    }
}
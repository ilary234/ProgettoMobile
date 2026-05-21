package com.example.progettoesame.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.progettoesame.ui.utils.AppPastelColor
import com.example.progettoesame.ui.utils.Theme
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val PASTEL_COLOR_KEY = stringPreferencesKey("pastel_color")
    }

    val theme = dataStore.data.map { preferences ->
        try {
            Theme.valueOf(preferences[THEME_KEY] ?: "Light")
        } catch (_: Exception) {
            Theme.Light
        }
    }

    val pastelColor = dataStore.data.map { preferences ->
        try {
            AppPastelColor.valueOf(preferences[PASTEL_COLOR_KEY] ?: "CREMA")
        } catch (_: Exception) {
            AppPastelColor.CREMA
        }
    }

    suspend fun setTheme(theme: Theme) = dataStore.edit { preferences ->
        preferences[THEME_KEY] = theme.toString()
    }

    suspend fun setPastelColor(color: AppPastelColor) = dataStore.edit { preferences ->
        preferences[PASTEL_COLOR_KEY] = color.toString()
    }
}
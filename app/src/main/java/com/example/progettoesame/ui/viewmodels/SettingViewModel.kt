package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.repositories.AuthRepository
import com.example.progettoesame.data.repositories.ThemeRepository
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeState(
    val theme: Theme,
    val dynamicColor: Boolean
)

data class ThemeActions(
    val setTheme: (Theme) -> Unit,
    val setDynamicColor: (Boolean) -> Unit
)

class SettingViewModel(private val authRepository: AuthRepository, private val themeRepository: ThemeRepository) : ViewModel() {
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
                AuthState.setLoggedOut()
                onSuccess() // Naviga solo dopo che lo stato è cambiato
                //clearError() //forse è da togliere
            } catch (e: Exception) {
                //_errorMessage.value = e.message ?: "Errore durante il logout"
            }
        }
    }

    val state = combine(
        themeRepository.theme,
        themeRepository.dynamicColor
    ) { theme, dynamicColor -> ThemeState(theme, dynamicColor) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ThemeState(Theme.Light, false)
        )

    val actions = ThemeActions(
        setTheme = { theme ->
            viewModelScope.launch { themeRepository.setTheme(theme)  }
        },
        setDynamicColor = { enabled ->
            viewModelScope.launch { themeRepository.setDynamicColor(enabled) }
        }
    )
}
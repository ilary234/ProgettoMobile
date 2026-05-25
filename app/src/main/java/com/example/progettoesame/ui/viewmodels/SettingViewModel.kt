package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.repositories.AuthRepository
import com.example.progettoesame.data.repositories.ThemeRepository
import com.example.progettoesame.ui.utils.AppPastelColor
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.utils.Theme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeState(
    val theme: Theme,
    val pastelColor: AppPastelColor
)

data class ThemeActions(
    val setTheme: (Theme) -> Unit,
    val setPastelColor: (AppPastelColor) -> Unit
)

class SettingViewModel(private val authRepository: AuthRepository, private val themeRepository: ThemeRepository) : ViewModel() {
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
                AuthState.setLoggedOut()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val state = combine(
        themeRepository.theme,
        themeRepository.pastelColor
    ) { theme, pastelColor -> ThemeState(theme, pastelColor) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ThemeState(Theme.Light, AppPastelColor.CREMA)
        )

    val actions = ThemeActions(
        setTheme = { theme ->
            viewModelScope.launch { themeRepository.setTheme(theme)  }
        },
        setPastelColor = { color ->
            viewModelScope.launch { themeRepository.setPastelColor(color) }
        }
    )
}
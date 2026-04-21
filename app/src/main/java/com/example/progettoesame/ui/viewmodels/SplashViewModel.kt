package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.repositories.SplashRepository
import com.example.progettoesame.ui.NavigationRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: SplashRepository) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _startDestination = MutableStateFlow<NavigationRoute?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        setup()
    }

    private fun setup() {
        viewModelScope.launch {
            delay(1000)
            val success = repository.prepareData()
            if (success) {
                _startDestination.value = NavigationRoute.Home
            } else {
                _startDestination.value = NavigationRoute.Error
            }
            _isReady.value = true
        }
    }
}
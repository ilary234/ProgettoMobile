package com.example.progettoesame.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.progettoesame.data.repositories.AuthRepository
import com.example.progettoesame.ui.utils.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isError = MutableStateFlow(true)
    val isError = _isError.asStateFlow()

    private val _currentUsername = MutableStateFlow("")
    val currentUsername = _currentUsername.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
        _isError.value = true
    }

    fun signUp(email: String, pass: String, username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.signUp(email, pass, username)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isError.value = true
            try {
                repository.login(email, pass)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Errore durante il login"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.signInWithGoogle()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Errore durante il login con Google"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.logout()
                AuthState.setLoggedOut()
                onSuccess() // Naviga solo dopo che lo stato è cambiato
                clearError() //forse è da togliere
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Errore durante il logout"
            }
        }
    }
}

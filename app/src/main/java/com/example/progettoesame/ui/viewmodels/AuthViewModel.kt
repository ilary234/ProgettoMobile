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

    fun getUsername() {
        viewModelScope.launch {
            try {
                val username = repository.getUsername()
                _currentUsername.value = username
            } catch (e: Exception) {
                _currentUsername.value = ""
            }
        }
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

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isError.value = true
            try {
                repository.resetPassword(email)
                _isError.value = false
                _errorMessage.value = "Email di reset inviata! Controlla la tua posta."
            } catch (e: Exception) {
                _isError.value = true
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePasswordFromReset(newP: String, confP: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.updatePassword(newP, confP)
                _isError.value = false
                onSuccess()
            } catch (e: Exception) {
                _isError.value = true
                _errorMessage.value = e.message ?: "Errore durante l'aggiornamento della password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePasswordStandard(oldP: String, newP: String, confP: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updatePassword(newP, confP, oldP)
                _isError.value = false
                onSuccess()
            } catch (e: Exception) {
                _isError.value = true
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(newEmail: String, newUsername: String, oldUsername: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentEmail = AuthState.userEmail.value
                val emailChanged = newEmail != currentEmail && newEmail.isNotBlank()
                val usernameChanged = newUsername != oldUsername && newUsername.isNotBlank()

                repository.updateProfile(
                    newEmail = if (emailChanged) newEmail else null,
                    newUsername = if (usernameChanged) newUsername else null
                )

                if (emailChanged) {
                    _isError.value = false
                    _errorMessage.value = "Controlla la tua posta per confermare la nuova email!"
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                _isError.value = true
                _errorMessage.value = when {
                    e.message?.contains("A user with this email address has already been registered") == true ->
                        "Questa email è già associata a un altro account."
                    e.message?.contains("User already exists") == true ->
                        "Utente già registrato."
                    e.message?.contains("invalid format") == true ->
                        "Inserisci un indirizzo email valido"
                    e.message?.contains("For security purposes, you") == true -> {
                        val seconds = e.message?.filter { it.isDigit() } ?: ""
                        "Per motivi di sicurezza, potrai riprovare tra $seconds secondi."
                    }
                    e.message?.contains("rate limit") == true ->
                        "Troppe richieste in breve tempo. Riprova tra poco."
                    else -> "${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

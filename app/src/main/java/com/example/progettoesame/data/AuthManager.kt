package com.example.progettoesame.data

import com.example.progettoesame.ui.utils.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AuthManager(private val supabase: SupabaseClient) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        observeSession()
    }

    private fun observeSession() {
        supabase.auth.sessionStatus.onEach { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    if (user != null) {
                        AuthState.setLoggedIn(user.id, user.email!!)
                    }
                }
                is SessionStatus.NotAuthenticated -> {
                    AuthState.setLoggedOut()
                }
                else -> {}
            }
        }.launchIn(scope)
    }
}
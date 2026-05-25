package com.example.progettoesame.data

import com.example.progettoesame.ui.utils.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

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
                        scope.launch {
                            val username = try {
                                val userRow = supabase.from("users")
                                    .select { filter { eq("user_id", user.id) } }
                                    .decodeSingleOrNull<JsonObject>()
                                userRow?.get("username")?.toString()?.replace("\"", "")
                            } catch (_: Exception) {
                                null
                            }

                            AuthState.setLoggedIn(
                                id = user.id,
                                email = user.email!!,
                                username = username!!,
                                isResetMode = AuthState.isResetPasswordMode.value
                            )
                        }
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
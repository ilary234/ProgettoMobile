package com.example.progettoesame.data.repositories

import com.example.progettoesame.ui.utils.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(private val supabase: SupabaseClient) {
    suspend fun signUp(email: String, pass: String, username: String) {
        if (email.isBlank() || pass.isBlank() || username.isBlank()) {
            throw Exception("Compila tutti i campi")
        }

        if (pass.length < 6) {
            throw Exception("La password deve contenere almeno 6 caratteri")
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw Exception("Inserisci un indirizzo email valido")
        }

        val existingUser = supabase.from("users")
            .select {
                filter { eq("username", username) }
            }.decodeSingleOrNull<JsonObject>()

        if (existingUser != null) {
            throw Exception("Lo username è già stato utilizzato")
        }

        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                data = buildJsonObject { put("username", username) }
            }
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("User already registered") == true -> "L'utente esiste già con questa email"
                //e.message?.contains("Password should be") == true -> "La password è troppo corta (minimo 6 caratteri)"
                else -> e.message ?: "Errore durante la registrazione"
            }
            throw Exception(message)
        }

        val userId = supabase.auth.currentUserOrNull()?.id
            ?: supabase.auth.retrieveUserForCurrentSession(true).id

        supabase.from("users").upsert(buildJsonObject {
            put("user_id", userId)
            put("username", username)
            put("email", email)
        })
    }

    suspend fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            throw Exception("Compila tutti i campi")
        }

        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                password = pass
            }
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Invalid login credentials") == true -> "Email o password errati"
                e.message?.contains("Email not confirmed") == true -> "Verifica la tua email prima di accedere"
                e.message?.contains("network") == true -> "Errore di connessione. Riprova"
                else -> "Impossibile accedere: controlla i tuoi dati"
            }
            throw Exception(message)
        }
    }

    suspend fun signInWithGoogle() {
        supabase.auth.signInWith(Google) {
            queryParams.putAll(mapOf("prompt" to "select_account"))
        }
    }

    suspend fun updatePassword(newPass: String, confirmPass: String, oldPass: String? = null) {
        if (newPass.isBlank() || confirmPass.isBlank()) {
            throw Exception("Compila tutti i campi")
        }
        if (newPass == oldPass) {
            throw Exception("La nuova password non può essere uguale a quella attuale")
        }
        if (newPass.length < 6) {
            throw Exception("La password deve contenere almeno 6 caratteri")
        }
        if (newPass != confirmPass) {
            throw Exception("Le password non coincidono")
        }

        try {
            oldPass?.let {
                if (it.isBlank()) throw Exception("Compila tutti i campi")

                val email = AuthState.userEmail.value ?: throw Exception("Sessione scaduta")

                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = it
                }
            }
            supabase.auth.updateUser {
                password = newPass
            }
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("Invalid login credentials") == true -> "La password attuale è errata"
                e.message?.contains("network") == true -> "Errore di connessione"
                else -> e.message ?: "Errore durante l'aggiornamento"
            }
            throw Exception(message)
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }
}

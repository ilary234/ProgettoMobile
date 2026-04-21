package com.example.progettoesame.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.utils.AuthScreenTemplate

@Composable
fun SignUpScreen(navController: NavHostController, onNavigateToLogin: () -> Unit) {
    AuthScreenTemplate(
        title = "Crea un account",
        subtitle = "Inserisci i tuoi dati per registrarti",
        buttonText = "Registrati",
        socialGoogleText = "Continua con Google",
        socialAppleText = "Continua con Apple",
        onButtonClick = { email, pass, user -> /* Registrazione con username */ },
        footerText = buildAnnotatedString {
            append("Hai già un account? "); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Accedi") }
        },
        onFooterClick = onNavigateToLogin,
        isSignUp = true
    )
}
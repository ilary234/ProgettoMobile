package com.example.progettoesame.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.utils.AuthScreenTemplate

@Composable
fun LoginScreen(navController: NavHostController, onNavigateToSignUp: () -> Unit) {
    AuthScreenTemplate(
        title = "Bentornato",
        subtitle = "Accedi per continuare",
        buttonText = "Accedi",
        onButtonClick = { email, pass, _ -> /* Login */ },
        footerText = buildAnnotatedString {
            append("Non hai un account? "); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Registrati") }
        },
        onFooterClick = onNavigateToSignUp
    )
}
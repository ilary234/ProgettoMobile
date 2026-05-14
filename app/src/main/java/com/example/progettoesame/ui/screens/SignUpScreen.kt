package com.example.progettoesame.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.AuthScreenTemplate
import com.example.progettoesame.ui.utils.FeedbackBanner
import com.example.progettoesame.ui.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val errorMessage by authViewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(4000)
            authViewModel.clearError()
        }
    }

    AuthScreenTemplate(
        onBackClick = { navController.navigateUp() },
        title = "Crea un account",
        subtitle = "Inserisci i tuoi dati per registrarti",
        buttonText = "Registrati",
        onSocialGoogleClick = { authViewModel.signInWithGoogle() },
        onButtonClick = { email, pass, user ->
            authViewModel.signUp(email, pass, user) {
                navController.navigate(NavigationRoute.Home) {
                    popUpTo(NavigationRoute.Login) { inclusive = true }
                }
            }
        },
        footerText = buildAnnotatedString {
            append("Hai già un account? "); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Accedi") }
        },
        onFooterClick = { navController.navigateUp() },
        isSignUp = true
    )

    FeedbackBanner(
        message = errorMessage ?: "",
        isVisible = errorMessage != null
    )
}
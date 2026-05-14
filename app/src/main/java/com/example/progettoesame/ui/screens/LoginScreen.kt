package com.example.progettoesame.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.utils.AuthScreenTemplate
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    AuthScreenTemplate(
        onBackClick = { navController.navigateUp() },
        title = "Bentornato",
        subtitle = "Accedi per continuare",
        buttonText = "Accedi",
        onSocialGoogleClick = { authViewModel.signInWithGoogle() },
        onButtonClick = { email, pass, _ ->
            authViewModel.login(email, pass) {
                navController.navigate(NavigationRoute.Home) {
                    popUpTo(NavigationRoute.Login) { inclusive = true }
                }
            }
        },
        footerText = buildAnnotatedString {
            append("Non hai un account? "); withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Registrati") }
        },
        onFooterClick = { navController.navigate(NavigationRoute.SignUp) }
    )
}
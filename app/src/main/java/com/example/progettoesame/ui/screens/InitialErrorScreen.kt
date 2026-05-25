package com.example.progettoesame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.progettoesame.ui.NavigationRoute
import com.example.progettoesame.ui.viewmodels.InitialErrorViewModel
import kotlinx.coroutines.delay

@Composable
fun InitialErrorScreen(navController: NavHostController, initialErrorViewModel: InitialErrorViewModel) {
    val isReady by initialErrorViewModel.isReady.collectAsStateWithLifecycle()

    val appBackgroundColor = MaterialTheme.colorScheme.background
    val appTextColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(isReady) {
        if (isReady) {
            navController.navigate(NavigationRoute.Home) {
                popUpTo(NavigationRoute.Error) { inclusive = true }
            }
        }
    }
    LaunchedEffect(Unit) {
        while (!isReady) {
            initialErrorViewModel.retry()
            delay(5000)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = appBackgroundColor
    ) {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Per il primo avvio è necessario essere connessi a Internet.",
                textAlign = TextAlign.Center,
                color = appTextColor,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tentativo di riconnessione in corso...",
                textAlign = TextAlign.Center,
                color = appTextColor.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = appTextColor
            )
        }
    }
}
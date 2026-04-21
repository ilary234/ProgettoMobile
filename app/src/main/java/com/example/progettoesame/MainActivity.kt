package com.example.progettoesame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.progettoesame.ui.NavGraph
import com.example.progettoesame.ui.theme.ProgettoEsameTheme
import com.example.progettoesame.ui.viewmodels.SplashViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }
        setContent {
            ProgettoEsameTheme {
                val navController = rememberNavController()
                val startDestination by splashViewModel.startDestination.collectAsStateWithLifecycle()
                startDestination?.let { destination ->
                    NavGraph(
                        navController = navController,
                        startDestination = destination
                    )
                }
            }
        }
    }
}

package com.example.progettoesame

import android.content.Intent
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
import com.example.progettoesame.ui.utils.AuthState
import com.example.progettoesame.ui.viewmodels.SplashViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModel()
    private val supabaseClient: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        handleIncomingIntent(intent)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }
        setContent {
            KoinContext {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        intent?.data?.let { data ->
            supabaseClient.handleDeeplinks(intent)
            val fullUrl = data.toString()
            if (fullUrl.contains("type=recovery") || fullUrl.contains("recovery")) {
                AuthState.enableResetModeOnly()
            }
        }
    }
}

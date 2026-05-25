package com.example.progettoesame.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.progettoesame.ui.utils.AppPastelColor

@Composable
fun ProgettoEsameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pastelColor: AppPastelColor = AppPastelColor.CREMA,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = Color.Black,
            onBackground = Color.White,
            surface = pastelColor.darkColor,
            surfaceVariant = pastelColor.lightColor,
            outline = Color(0x99FFFFFF),
            outlineVariant = Color(0xFF303030),
            surfaceContainer = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            background = Color.White,
            onBackground = Color.Black,
            surface = pastelColor.lightColor,
            surfaceVariant = pastelColor.darkColor,
            outline = Color.Gray,
            outlineVariant = Color(0xFFE0E0E0),
            surfaceContainer = Color(0xFFF2F2F2)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
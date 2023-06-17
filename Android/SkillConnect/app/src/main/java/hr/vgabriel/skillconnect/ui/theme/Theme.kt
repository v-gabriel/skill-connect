package hr.vgabriel.skillconnect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SkillConnectTheme(content: @Composable () -> Unit) {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val lightColors = lightColors(
        primary = Color(0xFF1976D2),
        primaryVariant = Color(0xFF1565C0),
        secondary = Color(0xFF03DAC6),
        secondaryVariant = Color(0xFF018786),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF5F5F5),
        error = Color(0xFFB00020),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White
    )

    val darkColors = darkColors(
        primary = Color(0xFF0A7FDD),
        primaryVariant = Color(0xFF0C6ECE),
        secondary = Color(0xFF03DAC6),
        secondaryVariant = Color(0xFF018786),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        error = Color(0xFFCF6679),
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.White
    )


    val colors = if (isSystemInDarkTheme) {
        darkColors
    } else {
        lightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()

            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !isSystemInDarkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !isSystemInDarkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !isSystemInDarkTheme
        }
    }

    MaterialTheme(
        colors = colors,
        content = content,
    )
}

package mx.edu.utng.jdrj.disponibilidad.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    secondary = UTNGGreenAccent,
    tertiary = UTNGBluePrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurface,
    onPrimary = DarkBackground,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = UTNGBluePrimary,
    secondary = UTNGGreenAccent,
    tertiary = UTNGBlueLight,
    background = FondoGrisNube,

    // Tarjetas y Superficies
    surface = CardBackground,
    surfaceVariant = CardBackground,

    // Textos y contenido sobre colores
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextPrimary
)

@Composable
fun DisponibilidadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = false, // Lo ponemos en false para forzar los colores UTNG
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pintar la barra de estado del color primario (Azul UTNG)
            window.statusBarColor = colorScheme.primary.toArgb()
            // Controlar si los iconos de la barra son blancos o negros
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Usa la tipografía por defecto de Type.kt
        content = content
    )
}
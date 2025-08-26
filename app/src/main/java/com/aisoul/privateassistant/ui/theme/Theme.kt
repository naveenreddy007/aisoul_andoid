package com.aisoul.privateassistant.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ✨ GOD-LEVEL PREMIUM COLOR SCHEMES ✨
private val DivineDarkColorScheme = darkColorScheme(
    primary = DivinePurple,
    onPrimary = PlatinumWhite,
    primaryContainer = CosmicNavy,
    onPrimaryContainer = StardustSilver,
    
    secondary = CelestialBlue,
    onSecondary = PlatinumWhite,
    secondaryContainer = ShadowGray,
    onSecondaryContainer = StardustSilver,
    
    tertiary = MysticViolet,
    onTertiary = PlatinumWhite,
    tertiaryContainer = ShadowGray,
    onTertiaryContainer = StardustSilver,
    
    background = VoidBlack,
    onBackground = StardustSilver,
    surface = CosmicNavy,
    onSurface = PlatinumWhite,
    surfaceVariant = ShadowGray,
    onSurfaceVariant = MoonbeamGray,
    
    error = RubyRed,
    onError = PlatinumWhite,
    errorContainer = RubyRed.copy(alpha = 0.2f),
    onErrorContainer = RubyRed,
    
    outline = MoonbeamGray,
    outlineVariant = ShadowGray
)

private val CelestialLightColorScheme = lightColorScheme(
    primary = DivinePurple,
    onPrimary = PlatinumWhite,
    primaryContainer = StardustSilver,
    onPrimaryContainer = CosmicNavy,
    
    secondary = CelestialBlue,
    onSecondary = PlatinumWhite,
    secondaryContainer = StardustSilver.copy(alpha = 0.3f),
    onSecondaryContainer = ShadowGray,
    
    tertiary = MysticViolet,
    onTertiary = PlatinumWhite,
    tertiaryContainer = MysticViolet.copy(alpha = 0.1f),
    onTertiaryContainer = MysticViolet,
    
    background = PlatinumWhite,
    onBackground = VoidBlack,
    surface = StardustSilver.copy(alpha = 0.1f),
    onSurface = VoidBlack,
    surfaceVariant = StardustSilver.copy(alpha = 0.3f),
    onSurfaceVariant = ShadowGray,
    
    error = RubyRed,
    onError = PlatinumWhite,
    errorContainer = RubyRed.copy(alpha = 0.1f),
    onErrorContainer = RubyRed,
    
    outline = MoonbeamGray,
    outlineVariant = StardustSilver
)


// ✨ GOD-LEVEL AI SOUL THEME WITH FROZEN EFFECTS ✨
@Composable
fun AISoulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 🌌 Dynamic color with enhanced god-level effects
    dynamicColor: Boolean = true,
    // ❄️ Frozen glass effect toggle
    enableFrozenEffects: Boolean = true,
    content: @Composable () -> Unit
) {
    // 🔮 Select divine color scheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                // Enhanced dynamic dark with god-level tweaks
                dynamicDarkColorScheme(context).copy(
                    primary = DivinePurple,
                    secondary = CelestialBlue,
                    tertiary = MysticViolet,
                    background = VoidBlack,
                    surface = CosmicNavy
                )
            } else {
                // Enhanced dynamic light with celestial touches
                dynamicLightColorScheme(context).copy(
                    primary = DivinePurple,
                    secondary = CelestialBlue,
                    tertiary = MysticViolet
                )
            }
        }
        darkTheme -> DivineDarkColorScheme
        else -> CelestialLightColorScheme
    }
    
    // 🌌 God-level window configuration
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // ✨ Premium status bar with gradient effect
            window.statusBarColor = if (darkTheme) {
                VoidBlack.copy(alpha = 0.8f).toArgb()
            } else {
                PlatinumWhite.copy(alpha = 0.95f).toArgb()
            }
            
            // 🌟 Navigation bar enhancement
            window.navigationBarColor = if (darkTheme) {
                CosmicNavy.copy(alpha = 0.9f).toArgb()
            } else {
                StardustSilver.copy(alpha = 0.7f).toArgb()
            }
            
            // 💥 Premium light/dark status bar icons
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            
            // ❄️ Enable frozen glass blur effect (API 31+)
            if (enableFrozenEffects && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    window.setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                    )
                } catch (e: Exception) {
                    // Graceful fallback for unsupported devices
                }
            }
        }
    }

    // 🎨 Apply god-level Material Theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography, // Enhanced typography
        content = content
    )
}
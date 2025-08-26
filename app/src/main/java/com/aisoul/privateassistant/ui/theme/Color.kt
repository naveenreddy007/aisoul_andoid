package com.aisoul.privateassistant.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// ✨ GOD-LEVEL PREMIUM COLOR PALETTE ✨
// 🌟 Primary Colors - Divine Blues & Purples
val DivinePurple = Color(0xFF6366F1)  // Premium indigo
val CelestialBlue = Color(0xFF3B82F6)  // Premium blue
val MysticViolet = Color(0xFF8B5CF6)   // Deep violet
val CosmicNavy = Color(0xFF1E1B4B)     // Deep space navy

// 🔮 Secondary Colors - Ethereal Grays & Silvers
val StardustSilver = Color(0xFFF1F5F9) // Light silver
val MoonbeamGray = Color(0xFF64748B)   // Medium gray
val ShadowGray = Color(0xFF334155)     // Dark gray
val VoidBlack = Color(0xFF0F172A)      // Premium black

// 💎 Accent Colors - Precious Metals & Gems
val PlatinumWhite = Color(0xFFFFFBFF)  // Pure white
val GoldAccent = Color(0xFFFBBF24)     // Premium gold
val EmeraldGreen = Color(0xFF10B981)   // Success green
val RubyRed = Color(0xFFEF4444)        // Error red
val AmberOrange = Color(0xFFF59E0B)    // Warning amber
val SapphireBlue = Color(0xFF0EA5E9)   // Info blue

// 🌊 Gradient Colors for God-Level Effects
val FrozenGlassStart = Color(0x40FFFFFF) // 25% white overlay
val FrozenGlassEnd = Color(0x10FFFFFF)   // 6% white overlay
val ShadowOverlay = Color(0x20000000)    // 12% black shadow
val GlowOverlay = Color(0x30FFFFFF)      // 18% white glow

// ❄️ Frozen Background Gradients
val FrozenBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A).copy(alpha = 0.95f), // Deep navy with transparency
        Color(0xFF1E293B).copy(alpha = 0.90f), // Slate with transparency
        Color(0xFF334155).copy(alpha = 0.85f)  // Medium gray with transparency
    )
)

val FrozenGlassBrush = Brush.verticalGradient(
    colors = listOf(
        FrozenGlassStart,
        FrozenGlassEnd
    )
)

// 🎨 Legacy Colors (Maintained for compatibility)
val Purple80 = DivinePurple.copy(alpha = 0.8f)
val PurpleGrey80 = MoonbeamGray.copy(alpha = 0.8f)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = DivinePurple.copy(alpha = 0.6f)
val PurpleGrey40 = ShadowGray
val Pink40 = Color(0xFF7D5260)
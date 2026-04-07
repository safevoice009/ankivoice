package com.ankivoice.agent.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Palette (Slice-inspired)
val PrimaryColor = Color(0xFF6366F1) // Indigo/Violet accent
val AccentColor = Color(0xFFF43F5E) // Vibrant Rose
val SecondaryAccent = Color(0xFF10B981) // Emerald Green

// Neutrals for Light Mode
val PremiumWhite = Color(0xFFF9FAFB) // Soft White
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF111827) // Darkest Slate
val TextSecondary = Color(0xFF4B5563) // Medium Gray
val TextTertiary = Color(0xFF9CA3AF) // Light Gray

// Neutrals for Dark Mode
val DeepBlack = Color(0xFF0F172A) // Dark Navy/Black
val SurfaceDark = Color(0xFF1E293B)
val TextPrimaryDark = Color(0xFFF3F4F6)
val TextSecondaryDark = Color(0xFFD1D5DB)

// Legacy compatibility
val PureWhite = PremiumWhite
val Black = DeepBlack
val ToxicOrange = listOf(AccentColor, Color(0xFFFB923C))
val MidnightBlue = listOf(PrimaryColor, Color(0xFF3B82F6))
val DeepGlass = Color(0x33FFFFFF)
val LightGlass = Color(0x1AFFFFFF)
val GlossyBlack = Color(0x80000000)
val GlossyWhite = Color(0x80FFFFFF)

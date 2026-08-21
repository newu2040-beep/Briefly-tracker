package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemePalette(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    INDIGO("INDIGO", "Indigo Classic", IndigoPrimary, LavenderAccent),
    EMERALD("EMERALD", "Emerald Forest", EmeraldPrimary, MintAccent),
    OCEAN("OCEAN", "Ocean Azure", OceanPrimary, CyanAccent),
    SUNSET("SUNSET", "Sunset Amber", SunsetPrimary, CoralWarmAccent),
    AMETHYST("AMETHYST", "Amethyst Dream", AmethystPrimary, VioletAccent),
    ROSE("ROSE", "Rose Quartz", RosePrimary, BerryAccent),
    MONOCHROME("MONOCHROME", "Onyx Minimal", Color(0xFF27272A), SlateAccent);

    companion object {
        fun fromId(id: String): AppThemePalette {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: INDIGO
        }
    }
}

fun getLightColorScheme(palette: AppThemePalette): ColorScheme {
    return when (palette) {
        AppThemePalette.INDIGO -> lightColorScheme(
            primary = IndigoPrimary,
            onPrimary = Color.White,
            primaryContainer = IndigoContainer,
            onPrimaryContainer = IndigoOnContainer,
            secondary = IndigoLight,
            onSecondary = Color.White,
            tertiary = EmeraldMindfulness,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            onBackground = LightOnSurface,
            onSurface = LightOnSurface,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = LightOutline,
            outlineVariant = Color(0xFFEDF2F7)
        )
        AppThemePalette.EMERALD -> lightColorScheme(
            primary = EmeraldPrimary,
            onPrimary = Color.White,
            primaryContainer = EmeraldContainer,
            onPrimaryContainer = EmeraldOnContainer,
            secondary = EmeraldLight,
            onSecondary = Color.White,
            tertiary = MintAccent,
            background = Color(0xFFF4FBF7),
            surface = LightSurface,
            surfaceVariant = Color(0xFFE8F5E9),
            onBackground = Color(0xFF0D2818),
            onSurface = Color(0xFF0D2818),
            onSurfaceVariant = Color(0xFF2E6044),
            outline = Color(0xFFC7E6D7),
            outlineVariant = Color(0xFFE0F2E9)
        )
        AppThemePalette.OCEAN -> lightColorScheme(
            primary = OceanPrimary,
            onPrimary = Color.White,
            primaryContainer = OceanContainer,
            onPrimaryContainer = OceanOnContainer,
            secondary = OceanLight,
            onSecondary = Color.White,
            tertiary = CyanAccent,
            background = Color(0xFFF0F7FF),
            surface = LightSurface,
            surfaceVariant = Color(0xFFE0F2FE),
            onBackground = Color(0xFF082F49),
            onSurface = Color(0xFF082F49),
            onSurfaceVariant = Color(0xFF336E94),
            outline = Color(0xFFBAE6FD),
            outlineVariant = Color(0xFFE0F2FE)
        )
        AppThemePalette.SUNSET -> lightColorScheme(
            primary = SunsetPrimary,
            onPrimary = Color.White,
            primaryContainer = SunsetContainer,
            onPrimaryContainer = SunsetOnContainer,
            secondary = SunsetLight,
            onSecondary = Color.White,
            tertiary = CoralWarmAccent,
            background = Color(0xFFFFFDF5),
            surface = LightSurface,
            surfaceVariant = Color(0xFFFEF3C7),
            onBackground = Color(0xFF451A03),
            onSurface = Color(0xFF451A03),
            onSurfaceVariant = Color(0xFF92400E),
            outline = Color(0xFFFDE68A),
            outlineVariant = Color(0xFFFEF3C7)
        )
        AppThemePalette.AMETHYST -> lightColorScheme(
            primary = AmethystPrimary,
            onPrimary = Color.White,
            primaryContainer = AmethystContainer,
            onPrimaryContainer = AmethystOnContainer,
            secondary = AmethystLight,
            onSecondary = Color.White,
            tertiary = VioletAccent,
            background = Color(0xFFFAF5FF),
            surface = LightSurface,
            surfaceVariant = Color(0xFFF3E8FF),
            onBackground = Color(0xFF3B0764),
            onSurface = Color(0xFF3B0764),
            onSurfaceVariant = Color(0xFF6B21A8),
            outline = Color(0xFFE9D5FF),
            outlineVariant = Color(0xFFF3E8FF)
        )
        AppThemePalette.ROSE -> lightColorScheme(
            primary = RosePrimary,
            onPrimary = Color.White,
            primaryContainer = RoseContainer,
            onPrimaryContainer = RoseOnContainer,
            secondary = RoseLight,
            onSecondary = Color.White,
            tertiary = BerryAccent,
            background = Color(0xFFFFF5F7),
            surface = LightSurface,
            surfaceVariant = Color(0xFFFCE7F3),
            onBackground = Color(0xFF500724),
            onSurface = Color(0xFF500724),
            onSurfaceVariant = Color(0xFF9D174D),
            outline = Color(0xFFFBCFE8),
            outlineVariant = Color(0xFFFCE7F3)
        )
        AppThemePalette.MONOCHROME -> lightColorScheme(
            primary = Color(0xFF18181B),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF4F4F5),
            onPrimaryContainer = Color(0xFF18181B),
            secondary = Color(0xFF52525B),
            onSecondary = Color.White,
            tertiary = Color(0xFF71717A),
            background = Color(0xFFFAFAFA),
            surface = LightSurface,
            surfaceVariant = Color(0xFFF4F4F5),
            onBackground = Color(0xFF18181B),
            onSurface = Color(0xFF18181B),
            onSurfaceVariant = Color(0xFF71717A),
            outline = Color(0xFFE4E4E7),
            outlineVariant = Color(0xFFF4F4F5)
        )
    }
}

fun getDarkColorScheme(palette: AppThemePalette): ColorScheme {
    return when (palette) {
        AppThemePalette.INDIGO -> darkColorScheme(
            primary = DarkIndigoPrimary,
            onPrimary = Color.White,
            primaryContainer = DarkIndigoContainer,
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = LavenderAccent,
            onSecondary = Color.White,
            tertiary = EmeraldMindfulness,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            onBackground = DarkOnSurface,
            onSurface = DarkOnSurface,
            onSurfaceVariant = DarkOnSurfaceVariant,
            outline = DarkOutline,
            outlineVariant = Color(0xFF2C324B)
        )
        AppThemePalette.EMERALD -> darkColorScheme(
            primary = EmeraldSoft,
            onPrimary = Color(0xFF064E3B),
            primaryContainer = Color(0xFF064E3B),
            onPrimaryContainer = Color(0xFFA7F3D0),
            secondary = MintAccent,
            onSecondary = Color.White,
            tertiary = EmeraldLight,
            background = Color(0xFF061A12),
            surface = Color(0xFF0F291E),
            surfaceVariant = Color(0xFF16382A),
            onBackground = Color(0xFFECFDF5),
            onSurface = Color(0xFFECFDF5),
            onSurfaceVariant = Color(0xFF6EE7B7),
            outline = Color(0xFF1F4E3C),
            outlineVariant = Color(0xFF16382A)
        )
        AppThemePalette.OCEAN -> darkColorScheme(
            primary = OceanSoft,
            onPrimary = Color(0xFF082F49),
            primaryContainer = Color(0xFF0C4A6E),
            onPrimaryContainer = Color(0xFFBAE6FD),
            secondary = CyanAccent,
            onSecondary = Color.White,
            tertiary = OceanLight,
            background = Color(0xFF041624),
            surface = Color(0xFF0B2438),
            surfaceVariant = Color(0xFF13344E),
            onBackground = Color(0xFFF0F9FF),
            onSurface = Color(0xFFF0F9FF),
            onSurfaceVariant = Color(0xFF7DD3FC),
            outline = Color(0xFF1C476A),
            outlineVariant = Color(0xFF13344E)
        )
        AppThemePalette.SUNSET -> darkColorScheme(
            primary = SunsetSoft,
            onPrimary = Color(0xFF451A03),
            primaryContainer = Color(0xFF78350F),
            onPrimaryContainer = Color(0xFFFDE68A),
            secondary = CoralWarmAccent,
            onSecondary = Color.White,
            tertiary = SunsetLight,
            background = Color(0xFF1A0F05),
            surface = Color(0xFF28180B),
            surfaceVariant = Color(0xFF3B2312),
            onBackground = Color(0xFFFFFBEB),
            onSurface = Color(0xFFFFFBEB),
            onSurfaceVariant = Color(0xFFFCD34D),
            outline = Color(0xFF55321B),
            outlineVariant = Color(0xFF3B2312)
        )
        AppThemePalette.AMETHYST -> darkColorScheme(
            primary = AmethystSoft,
            onPrimary = Color(0xFF3B0764),
            primaryContainer = Color(0xFF4C1D95),
            onPrimaryContainer = Color(0xFFE9D5FF),
            secondary = VioletAccent,
            onSecondary = Color.White,
            tertiary = AmethystLight,
            background = Color(0xFF130826),
            surface = Color(0xFF1E1038),
            surfaceVariant = Color(0xFF2B184F),
            onBackground = Color(0xFFFAF5FF),
            onSurface = Color(0xFFFAF5FF),
            onSurfaceVariant = Color(0xFFC084FC),
            outline = Color(0xFF422277),
            outlineVariant = Color(0xFF2B184F)
        )
        AppThemePalette.ROSE -> darkColorScheme(
            primary = RoseSoft,
            onPrimary = Color(0xFF500724),
            primaryContainer = Color(0xFF831843),
            onPrimaryContainer = Color(0xFFFBCFE8),
            secondary = BerryAccent,
            onSecondary = Color.White,
            tertiary = RoseLight,
            background = Color(0xFF1D0613),
            surface = Color(0xFF2C0B1E),
            surfaceVariant = Color(0xFF3E122C),
            onBackground = Color(0xFFFFF5F7),
            onSurface = Color(0xFFFFF5F7),
            onSurfaceVariant = Color(0xFFF472B6),
            outline = Color(0xFF5A1B40),
            outlineVariant = Color(0xFF3E122C)
        )
        AppThemePalette.MONOCHROME -> darkColorScheme(
            primary = Color(0xFFFAFAFA),
            onPrimary = Color(0xFF18181B),
            primaryContainer = Color(0xFF27272A),
            onPrimaryContainer = Color(0xFFF4F4F5),
            secondary = Color(0xFFA1A1AA),
            onSecondary = Color(0xFF18181B),
            tertiary = Color(0xFF71717A),
            background = Color(0xFF09090B),
            surface = Color(0xFF18181B),
            surfaceVariant = Color(0xFF27272A),
            onBackground = Color(0xFFFAFAFA),
            onSurface = Color(0xFFFAFAFA),
            onSurfaceVariant = Color(0xFFA1A1AA),
            outline = Color(0xFF3F3F46),
            outlineVariant = Color(0xFF27272A)
        )
    }
}

@Composable
fun BrieflyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paletteId: String = "INDIGO",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = AppThemePalette.fromId(paletteId)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> getDarkColorScheme(palette)
        else -> getLightColorScheme(palette)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

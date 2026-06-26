package com.tasbeeh.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TasbeehDarkColorScheme = darkColorScheme(
    primary            = PrimaryTeal,
    onPrimary          = OnPrimary,
    primaryContainer   = DarkSurface2,
    onPrimaryContainer = PrimaryTealLight,
    secondary          = PrimaryTealLight,
    onSecondary        = DarkBackground,
    background         = DarkBackground,
    onBackground       = OnDarkBackground,
    surface            = DarkSurface,
    onSurface          = OnDarkBackground,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = OnDarkMuted,
    outline            = DividerDark,
    error              = Error80,
    onError            = Error10,
    errorContainer     = Error10,
    onErrorContainer   = Error90
)

private val TasbeehLightColorScheme = lightColorScheme(
    primary            = PrimaryTeal,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryTealContainer,
    onPrimaryContainer = Green10,
    secondary          = Green40,
    onSecondary        = Neutral99,
    background         = LightBackground,
    onBackground       = OnLightSurface,
    surface            = LightSurface,
    onSurface          = OnLightSurface,
    surfaceVariant     = Neutral90,
    onSurfaceVariant   = Neutral10,
    error              = Error40,
    onError            = Neutral99,
    errorContainer     = Error90,
    onErrorContainer   = Error10
)

@Composable
fun TasbeehTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TasbeehDarkColorScheme else TasbeehLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = TasbeehTypography,
        content     = content
    )
}

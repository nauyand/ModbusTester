package com.teknosolvo.absensi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Success = Color(0xFF2E7D32)
val Warning = Color(0xFFE65100)

private val LightColors = lightColorScheme(primary = Color(0xFF2E7D32), secondary = Color(0xFF00695C))
private val DarkColors = darkColorScheme(primary = Color(0xFF81C784), secondary = Color(0xFF80CBC4))

@Composable
fun AbsensiTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}

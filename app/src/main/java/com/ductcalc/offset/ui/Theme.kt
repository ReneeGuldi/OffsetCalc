package com.ductcalc.offset.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(
    primary = Color(0xFF1F5FA8),
    tertiaryContainer = Color(0xFFFFF6C2), // yellow input cells, like the spreadsheet
    onTertiaryContainer = Color(0xFF3A3000),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9CC4FF),
    tertiaryContainer = Color(0xFF4A4220),
    onTertiaryContainer = Color(0xFFFFF0B0),
)

@Composable
fun OffsetCalcTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}

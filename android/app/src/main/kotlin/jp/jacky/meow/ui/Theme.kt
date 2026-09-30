package jp.jacky.meow.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** The app's coral, the iOS accent color. */
val Coral = Color(0xFFE76A66)
private val LightPink = Color(0xFFFFF6F7)
private val LightPinkDark = Color(0xFF2C2323)
private val LightYellow = Color(0xFFFFFFE0)
private val LightYellowDark = Color(0x31FFFFE0)

/** Background of a caption pill (the iOS lightPink color set). */
@Composable
fun pillColor(): Color = if (isSystemInDarkTheme()) LightPinkDark else LightPink

/** Background of the selected cat (the iOS lightYellow color set). */
@Composable
fun selectedColor(): Color = if (isSystemInDarkTheme()) LightYellowDark else LightYellow

@Composable
fun MeowTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) darkColorScheme(primary = Coral) else lightColorScheme(primary = Coral)
    MaterialTheme(colorScheme = scheme, content = content)
}

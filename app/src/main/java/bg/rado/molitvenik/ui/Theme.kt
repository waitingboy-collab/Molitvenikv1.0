package bg.rado.molitvenik.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Burgundy = Color(0xFF8B2E2E)
val Gold = Color(0xFFB08D57)
val Cream = Color(0xFFFBF7F0)

// Само светла тема — топъл, „хартиен“ вид като на молитвеник.
private val LightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E3D3),
    onPrimaryContainer = Color(0xFF4A1515),
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E9D2),
    onSecondaryContainer = Color(0xFF4A3410),
    background = Cream,
    onBackground = Color(0xFF2B2421),
    surface = Cream,
    onSurface = Color(0xFF2B2421),
    surfaceVariant = Color(0xFFF2ECE2),
    onSurfaceVariant = Color(0xFF5E5249),
    surfaceContainer = Color(0xFFF6F0E6),
    surfaceContainerLow = Color(0xFFFFFFFF),
    outline = Color(0xFFCBBFAF),
)

@Composable
fun MolitvenikTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

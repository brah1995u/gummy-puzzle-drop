package studio.cortex.gummypuzzledrop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CandyPink = Color(0xFFE83FA9)
val CandyHotPink = Color(0xFFFF59BC)
val CandyPurple = Color(0xFF8D22B5)
val CandyDeepPurple = Color(0xFF4C135E)
val CandyCream = Color(0xFFFFF4D2)
val CandyOrange = Color(0xFFFF9B24)
val CandyAqua = Color(0xFF52DDE4)
val CandyMint = Color(0xFFA7F3B7)
val InkPurple = Color(0xFF46145D)

private val scheme = darkColorScheme(
    primary = CandyHotPink,
    secondary = CandyOrange,
    background = CandyDeepPurple,
    surface = CandyPurple,
    onPrimary = CandyCream,
    onSecondary = InkPurple,
    onBackground = CandyCream,
    onSurface = CandyCream,
)

@Composable
fun GummyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

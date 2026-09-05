package studio.cortex.gummypuzzledrop.ui.gameplay

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.NormalPieceSpec
import studio.cortex.gummypuzzledrop.game.model.Rotation
import studio.cortex.gummypuzzledrop.game.model.ShapeDefinitions

@DrawableRes
fun candyDrawable(candy: CandyType): Int = when (candy) {
    CandyType.GREEN_BEAR -> R.drawable.gummy_green_bear
    CandyType.PURPLE_BEAR -> R.drawable.gummy_purple_bear
    CandyType.RED_BEAR -> R.drawable.gummy_red_bear
    CandyType.GREEN_STAR -> R.drawable.gummy_green_star
    CandyType.ORANGE_HEART -> R.drawable.gummy_orange_heart
    CandyType.PINK_BOMB -> R.drawable.gummy_pink_bomb
}

@Composable
fun CandyImage(candy: CandyType, modifier: Modifier = Modifier, alpha: Float = 1f) {
    Image(
        painter = painterResource(candyDrawable(candy)),
        contentDescription = candy.name.lowercase().replace('_', ' '),
        modifier = modifier,
        contentScale = ContentScale.Fit,
        alpha = alpha,
    )
}

@Composable
fun PiecePreview(spec: NormalPieceSpec?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        if (spec == null) return@BoxWithConstraints
        val offsets = ShapeDefinitions.offsets(spec.shape, Rotation.R0)
        val minX = offsets.minOf { it.x }
        val maxX = offsets.maxOf { it.x }
        val minY = offsets.minOf { it.y }
        val maxY = offsets.maxOf { it.y }
        val width = maxX - minX + 1
        val height = maxY - minY + 1
        val cell = minOf(maxWidth / 4, maxHeight / 4)
        val startX = (maxWidth - cell * width) / 2 - cell * minX
        val startY = (maxHeight - cell * height) / 2 - cell * minY
        Box(Modifier.fillMaxSize()) {
            offsets.zip(spec.candies).forEach { (position, candy) ->
                CandyImage(
                    candy = candy,
                    modifier = Modifier
                        .offset(x = startX + cell * position.x, y = startY + cell * position.y)
                        .size(cell),
                )
            }
        }
    }
}

package studio.cortex.gummypuzzledrop.ui.gameplay

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sign
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.game.engine.CollisionSystem
import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GamePhase
import studio.cortex.gummypuzzledrop.game.model.GameState
import studio.cortex.gummypuzzledrop.game.model.PieceCell
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyHotPink
import studio.cortex.gummypuzzledrop.ui.CandyPurple
import studio.cortex.gummypuzzledrop.ui.components.CandyDecoratedFrame

@Composable
fun GameBoard(
    state: GameState,
    onMove: (Int) -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier,
    showGhost: Boolean = true,
    reducedMotion: Boolean = false,
    boardTheme: String = "classic_board",
    effect: String = "clean_effect",
) {
    val resources = LocalContext.current.resources
    val bitmaps = remember(resources) {
        CandyType.entries.associateWith { candy ->
            ImageBitmap.imageResource(resources, candyDrawable(candy))
        }
    }
    val dangerAlpha = if (state.isDanger && !reducedMotion) {
        rememberInfiniteTransition(label = "danger").animateFloat(
            initialValue = .38f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(520), RepeatMode.Reverse),
            label = "dangerAlpha",
        ).value
    } else if (state.isDanger) .72f else 0f
    val shape = RoundedCornerShape(24.dp)
    val palette = boardPalette(boardTheme)

    Box(
        modifier = modifier.gameGestures(onMove, onRotate, onHardDrop),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(9.dp)
                .background(
                    Brush.verticalGradient(listOf(palette.frameTop, palette.frameBottom)),
                    RoundedCornerShape(18.dp),
                )
                .border(2.dp, palette.border, RoundedCornerShape(18.dp))
                .padding(3.dp)
                .clip(RoundedCornerShape(15.dp)),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val cell = size.width / GameConfig.BOARD_COLUMNS
                val rowHeight = size.height / GameConfig.VISIBLE_ROWS
                val inset = minOf(cell, rowHeight) * .055f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(palette.boardTop, palette.boardBottom),
                        startY = 0f,
                        endY = size.height,
                    ),
                    cornerRadius = CornerRadius(15.dp.toPx()),
                )
                for (column in 1 until GameConfig.BOARD_COLUMNS) {
                    val x = column * cell
                    drawLine(palette.grid.copy(alpha = .13f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                }
                for (row in 1 until GameConfig.VISIBLE_ROWS) {
                    val y = row * rowHeight
                    drawLine(palette.grid.copy(alpha = .09f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }

                clipRect(0f, 0f, size.width, size.height) {
                    state.board.cells.forEach { (position, candy) ->
                        if (position.y >= GameConfig.SPAWN_ROWS) {
                            drawCandy(bitmaps.getValue(candy), position.x.toFloat(), (position.y - GameConfig.SPAWN_ROWS).toFloat(), cell, rowHeight, inset)
                        }
                    }

                    val active = state.active
                    if (active != null) {
                        val ghost = CollisionSystem.landingPiece(active, state.board)
                        if (showGhost && ghost.origin != active.origin) {
                            ghost.cells().forEach { ghostCell ->
                                if (ghostCell.position.y >= GameConfig.SPAWN_ROWS) {
                                    drawCandy(
                                        bitmaps.getValue(ghostCell.candy),
                                        ghostCell.position.x.toFloat(),
                                        (ghostCell.position.y - GameConfig.SPAWN_ROWS).toFloat(),
                                        cell,
                                        rowHeight,
                                        inset,
                                        alpha = if (effect == "bubble_ghost") .31f else .20f,
                                    )
                                    drawGhostOutline(ghostCell, cell, rowHeight, effect)
                                }
                            }
                        }
                        active.cells().forEach { activeCell ->
                            if (activeCell.position.y >= GameConfig.SPAWN_ROWS) {
                                val scale = if (state.phase == GamePhase.LOCK_DELAY) .94f else 1f
                                drawActiveEffect(activeCell, cell, rowHeight, effect)
                                drawCandy(
                                    bitmaps.getValue(activeCell.candy),
                                    activeCell.position.x.toFloat(),
                                    (activeCell.position.y - GameConfig.SPAWN_ROWS).toFloat(),
                                    cell,
                                    rowHeight,
                                    inset,
                                    scale = scale,
                                )
                                if (effect == "sparkle_effect") drawSugarSparkle(activeCell, cell, rowHeight)
                            }
                        }
                    }
                }
            }
        }
        CandyDecoratedFrame(Modifier.matchParentSize())
        if (state.isDanger) {
            Box(
                Modifier
                    .matchParentSize()
                    .border(4.dp, CandyHotPink.copy(alpha = dangerAlpha), shape),
            )
        }
    }
}

private fun Modifier.gameGestures(
    onMove: (Int) -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
): Modifier = pointerInput(onMove, onRotate, onHardDrop) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var total = Offset.Zero
        var horizontalRemainder = 0f
        var horizontalSteps = 0
        var latest = down
        val gridStep = size.width / GameConfig.BOARD_COLUMNS

        do {
            val event = awaitPointerEvent()
            latest = event.changes.firstOrNull { it.id == down.id } ?: break
            val delta = latest.positionChange()
            total += delta
            horizontalRemainder += delta.x
            while (abs(horizontalRemainder) >= gridStep * .72f) {
                val direction = horizontalRemainder.sign.toInt()
                onMove(direction)
                horizontalRemainder -= direction * gridStep * .72f
                horizontalSteps++
            }
            if (delta != Offset.Zero) latest.consume()
        } while (latest.pressed)

        val durationMs = (latest.uptimeMillis - down.uptimeMillis).coerceAtLeast(1L)
        val verticalVelocity = total.y / durationMs * 1_000f
        val isDownSwipe = total.y > gridStep * 1.25f &&
            total.y > abs(total.x) * 1.12f &&
            (verticalVelocity > 850f || total.y > gridStep * 3f)
        val isTap = horizontalSteps == 0 && total.getDistance() < viewConfiguration.touchSlop * 1.25f && durationMs < 360L
        when {
            isDownSwipe -> onHardDrop()
            isTap -> onRotate()
        }
    }
}

private fun DrawScope.drawCandy(
    bitmap: ImageBitmap,
    column: Float,
    row: Float,
    cellWidth: Float,
    cellHeight: Float,
    inset: Float,
    alpha: Float = 1f,
    scale: Float = 1f,
) {
    val targetWidth = (cellWidth - inset * 2) * scale
    val targetHeight = (cellHeight - inset * 2) * scale
    val left = column * cellWidth + (cellWidth - targetWidth) / 2
    val top = row * cellHeight + (cellHeight - targetHeight) / 2
    drawImage(
        image = bitmap,
        dstOffset = IntOffset(left.toInt(), top.toInt()),
        dstSize = IntSize(targetWidth.toInt().coerceAtLeast(1), targetHeight.toInt().coerceAtLeast(1)),
        alpha = alpha,
    )
}

private fun DrawScope.drawGhostOutline(cell: PieceCell, cellWidth: Float, cellHeight: Float, effect: String) {
    val left = cell.position.x * cellWidth + 2.dp.toPx()
    val top = (cell.position.y - GameConfig.SPAWN_ROWS) * cellHeight + 2.dp.toPx()
    drawRoundRect(
        color = if (effect == "bubble_ghost") CandyAqua.copy(alpha = .72f) else CandyCream.copy(alpha = .34f),
        topLeft = Offset(left, top),
        size = Size(cellWidth - 4.dp.toPx(), cellHeight - 4.dp.toPx()),
        cornerRadius = CornerRadius(8.dp.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(if (effect == "bubble_ghost") 2.dp.toPx() else 1.3.dp.toPx()),
    )
    if (effect == "bubble_ghost") {
        drawCircle(
            CandyCream.copy(alpha = .42f),
            radius = minOf(cellWidth, cellHeight) * .09f,
            center = Offset(left + cellWidth * .30f, top + cellHeight * .28f),
        )
    }
}

private fun DrawScope.drawActiveEffect(cell: PieceCell, cellWidth: Float, cellHeight: Float, effect: String) {
    if (effect != "heart_glow") return
    val center = Offset(
        (cell.position.x + .5f) * cellWidth,
        (cell.position.y - GameConfig.SPAWN_ROWS + .5f) * cellHeight,
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(CandyHotPink.copy(alpha = .38f), CandyHotPink.copy(alpha = 0f)),
            center = center,
            radius = minOf(cellWidth, cellHeight) * .68f,
        ),
        radius = minOf(cellWidth, cellHeight) * .68f,
        center = center,
    )
}

private fun DrawScope.drawSugarSparkle(cell: PieceCell, cellWidth: Float, cellHeight: Float) {
    val center = Offset(
        (cell.position.x + .73f) * cellWidth,
        (cell.position.y - GameConfig.SPAWN_ROWS + .24f) * cellHeight,
    )
    val radius = minOf(cellWidth, cellHeight) * .12f
    drawLine(CandyCream.copy(alpha = .90f), center - Offset(radius, 0f), center + Offset(radius, 0f), 1.5.dp.toPx())
    drawLine(CandyCream.copy(alpha = .90f), center - Offset(0f, radius), center + Offset(0f, radius), 1.5.dp.toPx())
    drawCircle(CandyAqua.copy(alpha = .72f), radius * .24f, center)
}

private data class BoardPalette(
    val frameTop: Color,
    val frameBottom: Color,
    val boardTop: Color,
    val boardBottom: Color,
    val grid: Color,
    val border: Color,
)

private fun boardPalette(id: String): BoardPalette = when (id) {
    "mint_frost" -> BoardPalette(
        Color(0xE8338F83), Color(0xEE185B69), Color(0xAA2A776F), Color(0xCC123D4B),
        Color(0xFFB9FFE7), CandyCream.copy(alpha = .90f),
    )
    "orange_pop" -> BoardPalette(
        Color(0xEEC85445), Color(0xE990306A), Color(0xB27D2746), Color(0xD14D1B49),
        Color(0xFFFFE199), Color(0xFFFFE9AD),
    )
    "aqua_dream" -> BoardPalette(
        Color(0xE82D7EAC), Color(0xEE503B9D), Color(0xAA276E9B), Color(0xCC2D245E),
        Color(0xFFA5F1FF), CandyAqua.copy(alpha = .95f),
    )
    else -> BoardPalette(
        Color(0xE8210C45), Color(0xEE45105F), Color(0xAA360B56), Color(0xCC1D0C3B),
        CandyAqua, CandyCream.copy(alpha = .88f),
    )
}

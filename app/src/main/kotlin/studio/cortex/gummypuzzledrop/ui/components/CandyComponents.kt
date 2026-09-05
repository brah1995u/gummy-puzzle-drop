package studio.cortex.gummypuzzledrop.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.roundToInt
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyHotPink
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.CandyOrange
import studio.cortex.gummypuzzledrop.ui.CandyPink
import studio.cortex.gummypuzzledrop.ui.CandyPurple
import studio.cortex.gummypuzzledrop.ui.InkPurple

private val panelShape = RoundedCornerShape(24.dp)

@Composable
fun CandyPanel(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val safePadding = if (contentPadding < 18.dp) 18.dp else contentPadding
    Box(
        modifier = modifier
            .shadow(13.dp, panelShape, ambientColor = CandyDeepPurple, spotColor = CandyDeepPurple),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(6.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFF45DC2), Color(0xFFB926A9))),
                    RoundedCornerShape(20.dp),
                ),
        )
        Box(Modifier.padding(safePadding), content = content)
        CandyDecoratedFrame(Modifier.matchParentSize())
    }
}

@Composable
fun CandyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orange: Boolean = false,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) .965f else 1f,
        animationSpec = tween(90),
        label = "candyButtonPress",
    )
    val colors = if (orange) {
        listOf(Color(0xFFFFE27C), Color(0xFFFFAF28), Color(0xFFFF762B))
    } else {
        listOf(Color(0xFFFFB4E1), Color(0xFFFF68C4), Color(0xFFBC35C4))
    }
    Box(
        modifier = modifier
            .height(58.dp)
            .shadow(9.dp, shape, spotColor = CandyDeepPurple.copy(alpha = .8f))
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 6.dp, vertical = 7.dp)
                .background(
                    Brush.verticalGradient(if (enabled) colors else colors.map { it.copy(alpha = .48f) }),
                    RoundedCornerShape(17.dp),
                ),
        )
        HorizontalCandyFrame(
            Modifier.matchParentSize().alpha(if (enabled) 1f else .48f),
        )
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp).size(39.dp),
                contentScale = ContentScale.Fit,
                alpha = if (enabled) 1f else .55f,
            )
        }
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = if (iconRes == null) 12.dp else 54.dp),
            color = CandyCream.copy(alpha = if (enabled) 1f else .65f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(InkPurple.copy(alpha = .8f), Offset(0f, 3f), 2f)),
        )
    }
}

@Composable
fun GameLogo(modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "GUMMY",
            color = CandyCream,
            fontSize = if (compact) 34.sp else 52.sp,
            lineHeight = if (compact) 34.sp else 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            style = TextStyle(
                shadow = Shadow(CandyPurple, Offset(0f, if (compact) 4f else 7f), if (compact) 2f else 4f),
            ),
        )
        Text(
            "PUZZLE DROP",
            color = Color(0xFFFFC745),
            fontSize = if (compact) 20.sp else 30.sp,
            lineHeight = if (compact) 22.sp else 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.4.sp,
            style = TextStyle(shadow = Shadow(InkPurple, Offset(0f, 4f), 2f)),
        )
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier.shadow(5.dp, shape, spotColor = CandyDeepPurple.copy(alpha = .65f)),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .background(Color(0xFF7D1C9C).copy(alpha = .94f), RoundedCornerShape(16.dp)),
        )
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(label.uppercase(), color = CandyAqua, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(value, color = CandyCream, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        HorizontalCandyFrame(Modifier.matchParentSize())
    }
}

@Composable
fun CandyToggle(
    label: String,
    description: String,
    marker: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    motionEnabled: Boolean = true,
    @DrawableRes iconRes: Int? = null,
) {
    val animationMs = if (motionEnabled) 180 else 0
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 35.dp else 3.dp,
        animationSpec = tween(animationMs),
        label = "toggleThumb",
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF52DFA3) else Color(0xFF57225F),
        animationSpec = tween(animationMs),
        label = "toggleTrack",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { stateDescription = if (checked) "On" else "Off" }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier
                .size(42.dp)
                .then(
                    if (iconRes == null) Modifier
                        .background(Brush.verticalGradient(listOf(CandyHotPink, CandyPurple)), CircleShape)
                        .border(1.5.dp, CandyCream.copy(alpha = .82f), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (iconRes != null) {
                Image(
                    painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(marker, color = CandyCream, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(label, color = CandyCream, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(
                description,
                color = CandyCream.copy(alpha = .70f),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            Modifier
                .width(72.dp)
                .height(40.dp)
                .shadow(4.dp, RoundedCornerShape(50), spotColor = CandyDeepPurple)
                .background(trackColor, RoundedCornerShape(50))
                .border(2.2.dp, CandyCream.copy(alpha = .90f), RoundedCornerShape(50))
        ) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(.72f)
                    .height(5.dp)
                    .padding(top = 2.dp)
                    .background(Color.White.copy(alpha = .22f), RoundedCornerShape(50)),
            )
            Text(
                if (checked) "ON" else "OFF",
                modifier = Modifier.align(if (checked) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 9.dp),
                color = CandyCream.copy(alpha = .88f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
            )
            Box(
                Modifier
                    .offset { IntOffset(thumbOffset.roundToPx(), 3.dp.roundToPx()) }
                    .size(34.dp)
                    .shadow(4.dp, CircleShape, spotColor = InkPurple.copy(alpha = .70f))
                    .background(
                        Brush.verticalGradient(
                            if (checked) listOf(Color(0xFFEFFFF9), CandyMint, Color(0xFF36C99A))
                            else listOf(Color.White, CandyCream, Color(0xFFFFB9DE)),
                        ),
                        CircleShape,
                    )
                    .border(1.7.dp, if (checked) CandyAqua else CandyPink.copy(alpha = .72f), CircleShape),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    Modifier.padding(top = 5.dp).width(15.dp).height(6.dp)
                        .background(Color.White.copy(alpha = .66f), RoundedCornerShape(50)),
                )
            }
        }
    }
}

@Composable
fun GummyImageButton(
    @DrawableRes drawable: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) .92f else 1f,
        animationSpec = tween(85),
        label = "gummyImageButtonPress",
    )
    Image(
        painter = painterResource(drawable),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    )
}

@Composable
fun GummyNavigationRow(
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 64.dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GummyImageButton(
            drawable = studio.cortex.gummypuzzledrop.R.drawable.ui_back_button,
            contentDescription = "Back",
            onClick = onBack,
            size = buttonSize,
        )
        GummyImageButton(
            drawable = studio.cortex.gummypuzzledrop.R.drawable.ui_home_button,
            contentDescription = "Home",
            onClick = onHome,
            size = buttonSize,
        )
    }
}

@Composable
fun CandyMiniButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    orange: Boolean = false,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null,
    badge: String? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) .96f else 1f,
        animationSpec = tween(90),
        label = "candyMiniButtonPress",
    )
    val labelSize = if (text.length >= 10) 9.sp else if (text.length >= 8) 10.sp else 12.sp
    val labelLineHeight = if (text.length >= 10) 10.sp else 13.sp
    val colors = if (orange) {
        listOf(Color(0xFFFFD45A), CandyOrange, Color(0xFFFF762B))
    } else {
        listOf(Color(0xFFFF91D6), CandyHotPink, CandyPurple)
    }
    Box(
        modifier
            .height(50.dp)
            .shadow(6.dp, shape, spotColor = CandyDeepPurple.copy(alpha = .72f))
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 5.dp, vertical = 6.dp)
                .background(
                    Brush.verticalGradient(if (enabled) colors else colors.map { it.copy(alpha = .44f) }),
                    RoundedCornerShape(14.dp),
                ),
        )
        HorizontalCandyFrame(
            Modifier.matchParentSize().alpha(if (enabled) 1f else .46f),
        )
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 7.dp).size(30.dp),
                contentScale = ContentScale.Fit,
                alpha = if (enabled) 1f else .52f,
            )
        }
        Text(
            text,
            modifier = Modifier.padding(horizontal = if (iconRes == null) 7.dp else 38.dp),
            color = CandyCream.copy(alpha = if (enabled) 1f else .62f),
            fontSize = labelSize,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = labelLineHeight,
            maxLines = 1,
            style = TextStyle(shadow = Shadow(InkPurple.copy(alpha = .7f), Offset(0f, 2f), 1f)),
        )
        if (badge != null) {
            Text(
                badge,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 5.dp)
                    .background(InkPurple.copy(alpha = .78f), RoundedCornerShape(50))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                color = CandyAqua,
                fontSize = 7.sp,
                lineHeight = 8.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun HorizontalCandyFrame(modifier: Modifier = Modifier) {
    val frame = ImageBitmap.imageResource(studio.cortex.gummypuzzledrop.R.drawable.ui_button_frame)
    Canvas(modifier) {
        val destinationHeight = size.height.roundToInt().coerceAtLeast(1)
        val destinationWidth = size.width.roundToInt().coerceAtLeast(1)
        val sourceCap = (frame.height * .50f).roundToInt().coerceAtLeast(1)
        val destinationCap = min(destinationHeight / 2, destinationWidth / 2).coerceAtLeast(1)
        val sourceMiddle = (frame.width - sourceCap * 2).coerceAtLeast(1)
        val destinationMiddle = (destinationWidth - destinationCap * 2).coerceAtLeast(1)

        drawImage(
            image = frame,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(sourceCap, frame.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(destinationCap, destinationHeight),
        )
        drawImage(
            image = frame,
            srcOffset = IntOffset(sourceCap, 0),
            srcSize = IntSize(sourceMiddle, frame.height),
            dstOffset = IntOffset(destinationCap, 0),
            dstSize = IntSize(destinationMiddle, destinationHeight),
        )
        drawImage(
            image = frame,
            srcOffset = IntOffset(frame.width - sourceCap, 0),
            srcSize = IntSize(sourceCap, frame.height),
            dstOffset = IntOffset(destinationWidth - destinationCap, 0),
            dstSize = IntSize(destinationCap, destinationHeight),
        )
    }
}

@Composable
fun CandyDecoratedFrame(modifier: Modifier = Modifier) {
    val frame = ImageBitmap.imageResource(studio.cortex.gummypuzzledrop.R.drawable.ui_panel_frame)
    Canvas(modifier) {
        val destinationWidth = size.width.roundToInt().coerceAtLeast(1)
        val destinationHeight = size.height.roundToInt().coerceAtLeast(1)
        val sourceCapX = (frame.width * .18f).roundToInt()
        val sourceCapY = (frame.height * .20f).roundToInt()
        val destinationCapX = min(44.dp.toPx().roundToInt(), destinationWidth / 2).coerceAtLeast(1)
        val destinationCapY = min(40.dp.toPx().roundToInt(), destinationHeight / 2).coerceAtLeast(1)
        val sourceRailX = frame.width / 2
        val sourceRailY = frame.height / 2
        val destinationMiddleWidth = (destinationWidth - destinationCapX * 2).coerceAtLeast(1)
        val destinationMiddleHeight = (destinationHeight - destinationCapY * 2).coerceAtLeast(1)

        fun slice(
            sourceX: Int,
            sourceY: Int,
            sourceWidth: Int,
            sourceHeight: Int,
            destinationX: Int,
            destinationY: Int,
            width: Int,
            height: Int,
        ) {
            drawImage(
                image = frame,
                srcOffset = IntOffset(sourceX, sourceY),
                srcSize = IntSize(sourceWidth.coerceAtLeast(1), sourceHeight.coerceAtLeast(1)),
                dstOffset = IntOffset(destinationX, destinationY),
                dstSize = IntSize(width.coerceAtLeast(1), height.coerceAtLeast(1)),
            )
        }

        slice(0, 0, sourceCapX, sourceCapY, 0, 0, destinationCapX, destinationCapY)
        slice(sourceRailX, 0, 2, sourceCapY, destinationCapX, 0, destinationMiddleWidth, destinationCapY)
        slice(frame.width - sourceCapX, 0, sourceCapX, sourceCapY, destinationWidth - destinationCapX, 0, destinationCapX, destinationCapY)
        slice(0, sourceRailY, sourceCapX, 2, 0, destinationCapY, destinationCapX, destinationMiddleHeight)
        slice(frame.width - sourceCapX, sourceRailY, sourceCapX, 2, destinationWidth - destinationCapX, destinationCapY, destinationCapX, destinationMiddleHeight)
        slice(0, frame.height - sourceCapY, sourceCapX, sourceCapY, 0, destinationHeight - destinationCapY, destinationCapX, destinationCapY)
        slice(sourceRailX, frame.height - sourceCapY, 2, sourceCapY, destinationCapX, destinationHeight - destinationCapY, destinationMiddleWidth, destinationCapY)
        slice(frame.width - sourceCapX, frame.height - sourceCapY, sourceCapX, sourceCapY, destinationWidth - destinationCapX, destinationHeight - destinationCapY, destinationCapX, destinationCapY)
    }
}

@Composable
fun CandyPreviewFrame(
    @DrawableRes drawable: Int,
    modifier: Modifier = Modifier,
) {
    val frame = ImageBitmap.imageResource(drawable)
    Canvas(modifier) {
        val destinationWidth = size.width.roundToInt().coerceAtLeast(1)
        val destinationHeight = size.height.roundToInt().coerceAtLeast(1)
        val sourceCapX = (frame.width * .20f).roundToInt()
        val sourceCapY = (frame.height * .20f).roundToInt()
        val destinationCapX = min(17.dp.toPx().roundToInt(), destinationWidth / 2).coerceAtLeast(1)
        val destinationCapY = min(17.dp.toPx().roundToInt(), destinationHeight / 2).coerceAtLeast(1)
        val sourceMiddleWidth = (frame.width - sourceCapX * 2).coerceAtLeast(1)
        val sourceMiddleHeight = (frame.height - sourceCapY * 2).coerceAtLeast(1)
        val destinationMiddleWidth = (destinationWidth - destinationCapX * 2).coerceAtLeast(1)
        val destinationMiddleHeight = (destinationHeight - destinationCapY * 2).coerceAtLeast(1)

        fun slice(
            sourceX: Int,
            sourceY: Int,
            sourceWidth: Int,
            sourceHeight: Int,
            destinationX: Int,
            destinationY: Int,
            width: Int,
            height: Int,
        ) {
            drawImage(
                image = frame,
                srcOffset = IntOffset(sourceX, sourceY),
                srcSize = IntSize(sourceWidth.coerceAtLeast(1), sourceHeight.coerceAtLeast(1)),
                dstOffset = IntOffset(destinationX, destinationY),
                dstSize = IntSize(width.coerceAtLeast(1), height.coerceAtLeast(1)),
            )
        }

        val sourceXs = intArrayOf(0, sourceCapX, frame.width - sourceCapX)
        val sourceYs = intArrayOf(0, sourceCapY, frame.height - sourceCapY)
        val sourceWidths = intArrayOf(sourceCapX, sourceMiddleWidth, sourceCapX)
        val sourceHeights = intArrayOf(sourceCapY, sourceMiddleHeight, sourceCapY)
        val destinationXs = intArrayOf(0, destinationCapX, destinationWidth - destinationCapX)
        val destinationYs = intArrayOf(0, destinationCapY, destinationHeight - destinationCapY)
        val destinationWidths = intArrayOf(destinationCapX, destinationMiddleWidth, destinationCapX)
        val destinationHeights = intArrayOf(destinationCapY, destinationMiddleHeight, destinationCapY)

        for (row in 0..2) {
            for (column in 0..2) {
                slice(
                    sourceXs[column],
                    sourceYs[row],
                    sourceWidths[column],
                    sourceHeights[row],
                    destinationXs[column],
                    destinationYs[row],
                    destinationWidths[column],
                    destinationHeights[row],
                )
            }
        }
    }
}

@Composable
fun CandyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    completed: Boolean = false,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .height(10.dp)
            .background(Color(0xFF52185F).copy(alpha = .78f), shape)
            .border(1.dp, CandyCream.copy(alpha = .42f), shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        if (completed) listOf(CandyAqua, CandyMint)
                        else listOf(CandyOrange, CandyHotPink),
                    ),
                    shape,
                ),
        )
    }
}

@Composable
fun PauseIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) .91f else 1f,
        animationSpec = tween(85),
        label = "pauseJellyPress",
    )
    Box(
        modifier
            .size(48.dp)
            .shadow(7.dp, CircleShape, spotColor = CandyDeepPurple)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(studio.cortex.gummypuzzledrop.R.drawable.ui_round_button),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        Canvas(Modifier.size(19.dp)) {
            val barWidth = size.width * .28f
            drawRoundRect(
                CandyCream,
                size = Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
            )
            drawRoundRect(
                CandyCream,
                topLeft = Offset(size.width - barWidth, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
            )
        }
    }
}

@Composable
fun TutorialGestureIcon(kind: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.size(42.dp)) {
        val stroke = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        when (kind) {
            0 -> {
                drawLine(CandyCream, Offset(5f, size.height / 2), Offset(size.width - 5f, size.height / 2), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(5f, size.height / 2), Offset(13f, size.height / 2 - 8f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(5f, size.height / 2), Offset(13f, size.height / 2 + 8f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(size.width - 5f, size.height / 2), Offset(size.width - 13f, size.height / 2 - 8f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(size.width - 5f, size.height / 2), Offset(size.width - 13f, size.height / 2 + 8f), strokeWidth = stroke.width, cap = stroke.cap)
            }
            1 -> {
                drawCircle(CandyCream, radius = size.minDimension * .34f, style = stroke)
                drawCircle(CandyOrange, radius = size.minDimension * .09f)
            }
            else -> {
                drawLine(CandyCream, Offset(size.width / 2, 5f), Offset(size.width / 2, size.height - 5f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(size.width / 2, size.height - 5f), Offset(size.width / 2 - 9f, size.height - 14f), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(CandyCream, Offset(size.width / 2, size.height - 5f), Offset(size.width / 2 + 9f, size.height - 14f), strokeWidth = stroke.width, cap = stroke.cap)
            }
        }
    }
}

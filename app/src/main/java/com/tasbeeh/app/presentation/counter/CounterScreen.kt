package com.tasbeeh.app.presentation.counter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.presentation.localization.LocalStrings
import kotlinx.coroutines.delay
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun CounterScreen(
    onNavigateToDhikrList: () -> Unit,
    viewModel: CounterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isGoalReached) {
        if (uiState.isGoalReached) {
            delay(1500)
            viewModel.onAutoReset()
        }
    }

    CounterContent(
        uiState = uiState,
        onTap = viewModel::onTap,
        onReset = viewModel::onReset,
        onSaveSession = viewModel::onSaveSession,
        onNavigateToDhikrList = onNavigateToDhikrList,
        onSetTarget = viewModel::onSetTarget
    )
}

@Composable
fun CounterContent(
    uiState: CounterUiState,
    onTap: () -> Unit,
    onReset: () -> Unit,
    onSaveSession: () -> Unit,
    onNavigateToDhikrList: () -> Unit,
    onSetTarget: (Int) -> Unit
) {
    val strings = LocalStrings.current

    val bgColor by animateColorAsState(
        targetValue = if (uiState.isGoalReached)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.background,
        animationSpec = tween(600),
        label = "bg"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToDhikrList) {
                Text(
                    text = uiState.selectedDhikr?.name ?: strings.selectDhikr,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.selectedDhikr?.arabicText?.let { arabic ->
                Text(
                    text = arabic,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            CounterButton(
                count = uiState.count,
                target = uiState.target,
                isGoalReached = uiState.isGoalReached,
                onClick = onTap
            )

            Spacer(modifier = Modifier.height(28.dp))

            PresetChipsRow(
                currentTarget = uiState.target,
                onSelectTarget = onSetTarget
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings.reset, style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onSaveSession) {
                    Text(strings.save, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun CounterButton(
    count: Int,
    target: Int,
    isGoalReached: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = tween(80),
        label = "press",
        finishedListener = { pressed = false }
    )

    val progress = if (target > 0) (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val buttonColor by animateColorAsState(
        targetValue = if (isGoalReached) secondaryColor else primaryColor,
        animationSpec = tween(400),
        label = "btn_color"
    )

    // Pulsating rings when goal is reached
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(270.dp)
            .scale(pressScale)
    ) {
        // Glow rings (always visible, stronger on goal)
        Box(
            modifier = Modifier
                .size(270.dp)
                .drawBehind {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseR = size.minDimension / 2f
                    if (isGoalReached) {
                        // Pulsating ring
                        drawCircle(
                            color = secondaryColor.copy(alpha = ringAlpha * 0.6f),
                            radius = baseR * ringScale,
                            center = center
                        )
                        drawCircle(
                            color = secondaryColor.copy(alpha = ringAlpha * 0.3f),
                            radius = baseR * ringScale * 1.15f,
                            center = center
                        )
                    }
                    // Soft glow layers
                    for (i in 1..3) {
                        drawCircle(
                            color = buttonColor.copy(alpha = 0.07f * (4 - i)),
                            radius = baseR + (i * 16).dp.toPx(),
                            center = center
                        )
                    }
                }
        )

        // Progress ring
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(270.dp),
            strokeWidth = 7.dp,
            strokeCap = StrokeCap.Round,
            color = buttonColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Gradient circle button
        Box(
            modifier = Modifier
                .size(224.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = 0.85f),
                            buttonColor
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(224.dp),
                shape = CircleShape,
                color = Color.Transparent,
                onClick = {
                    pressed = true
                    onClick()
                },
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { -it } + fadeIn(tween(120))) togetherWith
                                        (slideOutVertically { it } + fadeOut(tween(80)))
                            } else {
                                (slideInVertically { it } + fadeIn(tween(120))) togetherWith
                                        (slideOutVertically { -it } + fadeOut(tween(80)))
                            }
                        },
                        label = "counter_num"
                    ) { animCount ->
                        Text(
                            text = animCount.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChipsRow(
    currentTarget: Int,
    onSelectTarget: (Int) -> Unit
) {
    val presets = listOf(33 to "33", 99 to "99", 100 to "100", 0 to "∞")

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { (value, label) ->
            FilterChip(
                selected = currentTarget == value,
                onClick = { onSelectTarget(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

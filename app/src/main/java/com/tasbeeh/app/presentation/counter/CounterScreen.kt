package com.tasbeeh.app.presentation.counter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.domain.model.PredefinedBackgrounds
import com.tasbeeh.app.presentation.localization.LocalStrings
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------------------
// Colours (mirrored from spec so this file is self-contained)
// ---------------------------------------------------------------------------

private val DarkBackground   = Color(0xFF0D1B15)
private val DarkSurface      = Color(0xFF112218)
private val PrimaryTeal      = Color(0xFF1D9A6C)
private val PrimaryTealLight = Color(0xFF4ECBA0)
private val OnDarkMuted      = Color(0xFF7A9D8C)
private val InactiveBead     = Color(0xFF2A4A38)

// ---------------------------------------------------------------------------
// BeadRing
// ---------------------------------------------------------------------------

@Composable
fun BeadRing(
    count: Int,
    target: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryTeal,
    onClick: () -> Unit
) {
    val beadCount = 33
    val inactiveColor = accentColor.copy(alpha = 0.25f)
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        val center       = Offset(size.width / 2f, size.height / 2f)
        val ringRadius   = minOf(size.width, size.height) / 2f * 0.78f
        val beadRadiusPx = 6.dp.toPx()

        for (i in 0 until beadCount) {
            val angle = (2 * Math.PI * i / beadCount - Math.PI / 2).toFloat()
            val x     = center.x + ringRadius * cos(angle)
            val y     = center.y + ringRadius * sin(angle)
            val pos   = Offset(x, y)

            val isFilled  = count > 0 && i < count % beadCount
            val isCurrent = count > 0 && i == (count - 1) % beadCount

            when {
                isCurrent -> {
                    drawCircle(color = accentColor.copy(alpha = 0.35f), radius = beadRadiusPx * 3f, center = pos)
                    drawCircle(color = accentColor,                      radius = beadRadiusPx * 1.4f, center = pos)
                }
                isFilled  -> drawCircle(color = accentColor,   radius = beadRadiusPx, center = pos)
                else      -> drawCircle(color = inactiveColor, radius = beadRadiusPx, center = pos)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DhikrTabs — horizontal chip row
// ---------------------------------------------------------------------------

@Composable
fun DhikrTabs(
    dhikrs: List<Dhikr>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryTeal
) {
    if (dhikrs.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dhikrs.forEachIndexed { index, dhikr ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) accentColor else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = dhikr.name,
                    color = if (selected) Color.White else OnDarkMuted,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SegmentControl — "Дуа и Зикры" | "Тасбех"
// ---------------------------------------------------------------------------

@Composable
fun SegmentControl(
    selectedLabel: String,
    onDuaClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryTeal,
    surfaceColor: Color = DarkSurface
) {
    val strings = LocalStrings.current
    val options = listOf(strings.dhikrsTitle, strings.tasbeh)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(surfaceColor),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEach { label ->
            val isSelected = label == selectedLabel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) accentColor else Color.Transparent)
                    .clickable { if (label == strings.dhikrsTitle) onDuaClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    color      = if (isSelected) Color.White else OnDarkMuted,
                    fontSize   = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign  = TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CounterScreen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    onNavigateToDhikrList: () -> Unit = {},
    viewModel: CounterViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showTargetPicker by remember { mutableStateOf(false) }

    val bgTheme = remember(uiState.selectedBackgroundId) {
        PredefinedBackgrounds.find { it.id == uiState.selectedBackgroundId }
            ?: PredefinedBackgrounds.first()
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            snackbarHostState.showSnackbar(strings.dhikrComplete)
        }
    }

    if (showTargetPicker) {
        TasbihTypePickerSheet(
            currentTarget = uiState.target,
            sheetState    = sheetState,
            onDismiss     = { showTargetPicker = false },
            onSelect      = { viewModel.onEvent(CounterEvent.SetTarget(it)) }
        )
    }

    Scaffold(
        containerColor = bgTheme.bgColor,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData    = data,
                    containerColor  = bgTheme.accentColor,
                    contentColor    = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Segment toggle
            SegmentControl(
                selectedLabel = strings.tasbeh,
                onDuaClick    = onNavigateToDhikrList,
                accentColor   = bgTheme.accentColor,
                surfaceColor  = bgTheme.surfaceColor
            )

            // Dhikr tabs
            DhikrTabs(
                dhikrs        = uiState.dhikrs,
                selectedIndex = uiState.selectedDhikrIndex,
                onSelect      = { viewModel.onEvent(CounterEvent.SelectDhikr(it)) },
                accentColor   = bgTheme.accentColor
            )

            // Arabic text (if showTextOnScreen)
            uiState.selectedDhikr?.arabicText?.let { arabic ->
                Text(
                    text      = arabic,
                    color     = OnDarkMuted,
                    fontSize  = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            // Main bead ring + counter overlay
            Box(
                modifier           = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment   = Alignment.Center
            ) {
                BeadRing(
                    count       = uiState.count,
                    target      = uiState.target,
                    accentColor = bgTheme.accentColor,
                    onClick     = { viewModel.onEvent(CounterEvent.Increment) }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState  = uiState.count,
                        transitionSpec = {
                            (slideInVertically { -it } + fadeIn(tween(120))) togetherWith
                                    (slideOutVertically { it } + fadeOut(tween(80)))
                        },
                        label = "counter_anim"
                    ) { animCount ->
                        Text(
                            text       = animCount.toString(),
                            fontSize   = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }

                    Text(
                        text  = "/${uiState.target}",
                        color = OnDarkMuted,
                        fontSize = 20.sp
                    )
                }
            }

            // Bottom row: Reset + Target picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Reset button
                IconButton(
                    onClick  = { viewModel.onEvent(CounterEvent.Reset) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(bgTheme.surfaceColor)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Refresh,
                        contentDescription = strings.reset,
                        tint               = OnDarkMuted
                    )
                }

                // Target picker chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgTheme.surfaceColor)
                        .clickable { showTargetPicker = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Timer,
                            contentDescription = null,
                            tint               = bgTheme.accentColor,
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text       = "${strings.goal} ${uiState.target}",
                            color      = Color.White,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

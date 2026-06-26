package com.tasbeeh.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgColor      = Color(0xFF0D1B15)
private val TealAccent   = Color(0xFF1D9A6C)
private val TealDim      = Color(0xFF112218)
private val GoldAccent   = Color(0xFFD4AF37)
private val Muted        = Color(0xFF7A9D8C)

private const val SPLASH_DURATION_MS = 10_000

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = SPLASH_DURATION_MS, easing = LinearEasing)
        )
        onFinished()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated ring
            Box(
                modifier         = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke    = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    val inset     = stroke.width / 2
                    val arcSize   = Size(size.width - inset * 2, size.height - inset * 2)
                    val topLeft   = Offset(inset, inset)

                    // Background track
                    drawArc(
                        color       = TealDim,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = topLeft,
                        size        = arcSize,
                        style       = stroke
                    )
                    // Progress arc
                    drawArc(
                        color       = TealAccent,
                        startAngle  = -90f,
                        sweepAngle  = 360f * progress.value,
                        useCenter   = false,
                        topLeft     = topLeft,
                        size        = arcSize,
                        style       = stroke
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Arabic bismillah
                    Text(
                        text       = "بِسْمِ اللَّهِ",
                        color      = GoldAccent,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Percent
                    Text(
                        text       = "${(progress.value * 100).toInt()}%",
                        color      = Muted,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text       = "Тасбех",
                color      = Color.White,
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text     = "Дуа и Зикры",
                color    = Muted,
                fontSize = 15.sp
            )
        }
    }
}

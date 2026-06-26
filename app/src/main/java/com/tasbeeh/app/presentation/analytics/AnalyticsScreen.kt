package com.tasbeeh.app.presentation.analytics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.presentation.localization.AppStrings
import com.tasbeeh.app.presentation.localization.LocalStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

private val DarkBackground = Color(0xFF0D1B15)
private val DarkSurface    = Color(0xFF112218)
private val PrimaryTeal    = Color(0xFF1D9A6C)
private val OnDarkMuted    = Color(0xFF7A9D8C)
private val BarInactive    = Color(0xFF2A4A38)

private fun dayLabel(day: DayStats, strings: AppStrings): String {
    if (day.isToday) return strings.today
    return when (day.dayOfWeek) {
        Calendar.MONDAY    -> strings.dayMon
        Calendar.TUESDAY   -> strings.dayTue
        Calendar.WEDNESDAY -> strings.dayWed
        Calendar.THURSDAY  -> strings.dayThu
        Calendar.FRIDAY    -> strings.dayFri
        Calendar.SATURDAY  -> strings.daySat
        else               -> strings.daySun
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val state   by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(strings.analyticsTitle, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(title = strings.totalZikrs,          value = state.totalZikrs.toString(),        modifier = Modifier.weight(1f))
                        StatCard(title = strings.completedSessionsStat, value = state.completedSessions.toString(), modifier = Modifier.weight(1f))
                    }
                }

                item { WeekBarChart(stats = state.weekStats, strings = strings) }

                if (state.sessions.isNotEmpty()) {
                    item {
                        Text(
                            text       = strings.recentSessions,
                            color      = OnDarkMuted,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    items(state.sessions, key = { it.id }) { session ->
                        SessionRow(session = session, locale = strings.locale)
                    }
                } else {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = strings.noSessionsYet, color = OnDarkMuted, fontSize = 14.sp)
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(text = title, color = OnDarkMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun WeekBarChart(stats: List<DayStats>, strings: AppStrings) {
    val maxCount      = stats.maxOfOrNull { it.count } ?: 1
    val chartMaxHeight = 80.dp

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = strings.weekActivity, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.Bottom
            ) {
                stats.forEach { day ->
                    val fraction  = if (maxCount > 0) day.count.toFloat() / maxCount else 0f
                    val barHeight = (chartMaxHeight.value * fraction.coerceAtLeast(0.04f)).dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        if (day.count > 0) {
                            Text(text = day.count.toString(), color = PrimaryTeal, fontSize = 9.sp, modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (day.count > 0) PrimaryTeal else BarInactive)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(text = dayLabel(day, strings), color = OnDarkMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SessionRow(session: Session, locale: java.util.Locale) {
    val fmt = SimpleDateFormat("d MMM, HH:mm", locale)
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.dhikrName, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text(text = fmt.format(Date(session.timestamp)), color = OnDarkMuted, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "${session.count}/${session.target}", color = OnDarkMuted, fontSize = 13.sp)
                Icon(
                    imageVector        = if (session.completed) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint               = if (session.completed) PrimaryTeal else Color(0xFFE57373),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

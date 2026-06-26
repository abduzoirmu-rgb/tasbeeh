package com.tasbeeh.app.presentation.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Session
import com.tasbeeh.app.presentation.localization.LocalStrings
import java.text.SimpleDateFormat
import java.util.Date

private val DarkBackground = Color(0xFF0D1B15)
private val DarkSurface    = Color(0xFF112218)
private val PrimaryTeal    = Color(0xFF1D9A6C)
private val OnDarkMuted    = Color(0xFF7A9D8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val strings  = LocalStrings.current

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = strings.historyTitle,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            EmptyHistoryState(
                title = strings.noRecords,
                hint  = strings.completedZikrsHint,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(sessions, key = { it.id }) { session ->
                    HistorySessionCard(session = session, locale = strings.locale)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun HistorySessionCard(session: Session, locale: java.util.Locale) {
    val timeStr = SimpleDateFormat("d MMM, HH:mm", locale).format(Date(session.timestamp))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.dhikrName, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = timeStr, color = OnDarkMuted, fontSize = 12.sp)
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "${session.count}/${session.target}", color = OnDarkMuted, fontSize = 14.sp)
                Icon(
                    imageVector        = if (session.completed) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint               = if (session.completed) PrimaryTeal else Color(0xFFE57373),
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState(title: String, hint: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Default.History,
                contentDescription = null,
                modifier           = Modifier.size(72.dp),
                tint               = OnDarkMuted.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = OnDarkMuted, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = hint, color = OnDarkMuted.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}

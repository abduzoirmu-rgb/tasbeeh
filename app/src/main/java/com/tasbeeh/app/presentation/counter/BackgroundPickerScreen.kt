package com.tasbeeh.app.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.BackgroundTheme
import com.tasbeeh.app.domain.model.PredefinedBackgrounds
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.localization.localizedName
import com.tasbeeh.app.presentation.settings.SettingsViewModel

private val PageBg = Color(0xFF0D1B15)
private val Muted  = Color(0xFF7A9D8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()
    val strings  = LocalStrings.current

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.backgroundThemeTitle, color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns       = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(PredefinedBackgrounds) { theme ->
                BackgroundThemeCard(
                    theme    = theme,
                    name     = theme.localizedName(strings),
                    selected = settings.selectedBackgroundId == theme.id,
                    onClick  = {
                        settingsViewModel.updateSettings(
                            settings.copy(selectedBackgroundId = theme.id)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun BackgroundThemeCard(
    theme: BackgroundTheme,
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(theme.bgColor, theme.surfaceColor)))
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) theme.accentColor else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Mini bead ring preview
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(theme.accentColor.copy(alpha = 0.2f))
                    .border(2.dp, theme.accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "33",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selected) {
                Box(
                    modifier         = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }

        Text(
            text       = name,
            color      = if (selected) theme.accentColor else Muted,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

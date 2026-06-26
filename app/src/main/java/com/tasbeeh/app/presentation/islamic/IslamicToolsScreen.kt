package com.tasbeeh.app.presentation.islamic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.AllahName
import com.tasbeeh.app.domain.model.NinetynineNames
import com.tasbeeh.app.presentation.localization.LocalStrings

private val PageBg      = Color(0xFF0D1B15)
private val PageSurface = Color(0xFF112218)
private val TealAccent  = Color(0xFF1D9A6C)
private val GoldAccent  = Color(0xFFD4AF37)
private val Muted       = Color(0xFF7A9D8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicToolsScreen(
    onBack: () -> Unit,
    viewModel: IslamicToolsViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    val filtered = NinetynineNames.filter { name ->
        state.nameFilter.isEmpty() ||
        name.arabic.contains(state.nameFilter, ignoreCase = true) ||
        name.transliteration.contains(state.nameFilter, ignoreCase = true) ||
        name.meaningRu.contains(state.nameFilter, ignoreCase = true)
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = strings.names99title,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value         = state.nameFilter,
                onValueChange = viewModel::updateNameFilter,
                placeholder   = { Text(strings.namesSearchPlaceholder, color = Muted) },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = Muted) },
                singleLine    = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = TealAccent,
                    unfocusedBorderColor    = Muted.copy(alpha = 0.4f),
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    cursorColor             = TealAccent,
                    focusedContainerColor   = PageSurface,
                    unfocusedContainerColor = PageSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.number }) { name ->
                    AllahNameRow(name)
                    Divider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun AllahNameRow(name: AllahName) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TealAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = name.number.toString(),
                color      = TealAccent,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center
            )
        }

        Text(
            text       = name.arabic,
            color      = GoldAccent,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.End,
            modifier   = Modifier.width(110.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name.transliteration, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = name.meaningRu,       color = Muted,       fontSize = 11.sp)
        }
    }
}

package com.tasbeeh.app.presentation.dua

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.DuaItem

// ---------------------------------------------------------------------------
// Colours
// ---------------------------------------------------------------------------

private val LightBackground = Color(0xFFF2F4F3)
private val LightSurface    = Color(0xFFFFFFFF)
private val PrimaryTeal     = Color(0xFF1D9A6C)
private val ArabicColor     = Color(0xFF1A3A2A)
private val TranslitColor   = Color(0xFF4A6A5A)
private val TranslateColor  = Color(0xFF2A2A2A)
private val SourceColor     = Color(0xFF8A9A8A)
private val RepeatBg        = Color(0xFFE8F5F0)

// ---------------------------------------------------------------------------
// DuaDetailScreen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaDetailScreen(
    categoryId: Int,
    categoryTitle: String,
    onBack: () -> Unit,
    viewModel: DuaDetailViewModel = hiltViewModel(key = "dua_$categoryId")
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = categoryTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 18.sp,
                        color      = Color(0xFF1A1A1A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else if (uiState.duas.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "Дуа для этой категории\nпока не добавлены",
                    color     = SourceColor,
                    fontSize  = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(uiState.duas, key = { _, item -> item.id }) { index, dua ->
                    DuaCard(
                        dua         = dua,
                        index       = index + 1,
                        onFavorite  = { viewModel.toggleFavorite(dua.id, dua.isFavorite) }
                    )
                    if (index < uiState.duas.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(horizontal = 16.dp),
                            color     = Color(0xFFE8E8E8),
                            thickness = 1.dp
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DuaCard
// ---------------------------------------------------------------------------

@Composable
fun DuaCard(
    dua: DuaItem,
    index: Int,
    onFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightSurface)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Header row: number + favorite
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .background(RepeatBg, shape = RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = index.toString(),
                    color      = PrimaryTeal,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                dua.repeatCount?.let { count ->
                    Box(
                        modifier         = Modifier
                            .background(RepeatBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = "×$count",
                            color     = PrimaryTeal,
                            fontSize  = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector        = if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "В избранное",
                        tint               = if (dua.isFavorite) Color(0xFFE53935) else SourceColor,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Arabic text (RTL, large)
        Text(
            text       = dua.arabicText,
            color      = ArabicColor,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.End,
            lineHeight = 36.sp,
            modifier   = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Transliteration
        Text(
            text       = dua.transliteration,
            color      = TranslitColor,
            fontSize   = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Translation
        Text(
            text       = dua.translationRu,
            color      = TranslateColor,
            fontSize   = 14.sp,
            lineHeight = 22.sp
        )

        // Source
        dua.source?.let { src ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text     = src,
                color    = SourceColor,
                fontSize = 12.sp
            )
        }
    }
}

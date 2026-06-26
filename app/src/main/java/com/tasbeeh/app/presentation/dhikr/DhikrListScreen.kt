package com.tasbeeh.app.presentation.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.domain.model.Dhikr
import com.tasbeeh.app.presentation.localization.AppStrings
import com.tasbeeh.app.presentation.localization.LocalStrings

private val LightBackground = Color(0xFFF2F4F3)
private val LightSurface    = Color(0xFFFFFFFF)
private val PrimaryTeal     = Color(0xFF1D9A6C)
private val IconBgTeal      = Color(0xFFE8F5F0)

data class DhikrCategory(val title: String, val icon: ImageVector)

private val dhikrCategoryIcons = listOf(
    Icons.Default.WbSunny, Icons.Default.NightlightRound, Icons.Default.AutoAwesome,
    Icons.Default.Restaurant, Icons.Default.Bedtime, Icons.Default.AccountBalance,
    Icons.Default.Favorite, Icons.Default.Flight, Icons.Default.Star,
    Icons.Default.LocalFlorist, Icons.Default.FavoriteBorder, Icons.Default.Mosque,
    Icons.Default.MenuBook
)

private fun buildDhikrCategories(strings: AppStrings): List<DhikrCategory> =
    strings.duaCategories.take(13).zip(dhikrCategoryIcons).map { (name, icon) ->
        DhikrCategory(name, icon)
    }

@Composable
fun CategoryCard(category: DhikrCategory, onClick: () -> Unit = {}) {
    val strings = LocalStrings.current
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier         = Modifier.size(44.dp).clip(CircleShape).background(IconBgTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = category.icon,
                    contentDescription = category.title,
                    tint               = PrimaryTeal,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Text(text = category.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A1A), lineHeight = 18.sp)
            TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                Text(text = strings.open, color = PrimaryTeal, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrListScreen(
    selectedDhikrId: Long? = null,
    onSelectDhikr: (Dhikr) -> Unit = {},
    viewModel: DhikrViewModel = hiltViewModel()
) {
    val strings       = LocalStrings.current
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val allCategories = buildDhikrCategories(strings)
    val filtered      = allCategories.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = strings.dhikrsTitle, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder   = { Text(strings.searchPlaceholder, color = Color(0xFF9E9E9E)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9E9E9E)) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = LightSurface,
                    focusedContainerColor   = LightSurface,
                    focusedBorderColor      = PrimaryTeal,
                    unfocusedBorderColor    = Color(0xFFE0E0E0)
                )
            )
            LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.title }) { category ->
                    CategoryCard(category = category)
                }
            }
        }
    }
}

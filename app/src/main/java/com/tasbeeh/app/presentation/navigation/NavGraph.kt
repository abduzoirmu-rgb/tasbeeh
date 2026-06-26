package com.tasbeeh.app.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.presentation.analytics.AnalyticsScreen
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.counter.BackgroundPickerScreen
import com.tasbeeh.app.presentation.counter.CounterScreen
import com.tasbeeh.app.presentation.counter.CounterViewModel
import com.tasbeeh.app.presentation.dua.DuaDetailScreen
import com.tasbeeh.app.presentation.dua.DhikrListScreen
import com.tasbeeh.app.presentation.islamic.IslamicToolsScreen
import com.tasbeeh.app.presentation.premium.PremiumScreen
import com.tasbeeh.app.presentation.reminder.ReminderScreen
import com.tasbeeh.app.presentation.settings.SettingsScreen

// ---------------------------------------------------------------------------
// Bottom-tab destinations
// ---------------------------------------------------------------------------

enum class AppDestination(val icon: ImageVector) {
    Counter(Icons.Default.Loop),
    Analytics(Icons.Default.BarChart),
    Reminder(Icons.Default.Alarm),
    Settings(Icons.Default.Settings)
}

// Navigation sub-state: what the "full-screen" overlay shows on top of tabs
sealed interface OverlayScreen {
    data object None : OverlayScreen
    data class DhikrList(val returnToCounter: Boolean = true) : OverlayScreen
    data class DuaDetail(val categoryId: Int, val categoryTitle: String) : OverlayScreen
    data object BackgroundPicker : OverlayScreen
    data object IslamicTools : OverlayScreen
    data object Premium : OverlayScreen
}

// ---------------------------------------------------------------------------
// Colours
// ---------------------------------------------------------------------------

private val NavBackground  = Color(0xFF112218)
private val NavSelected    = Color(0xFF1D9A6C)
private val NavUnselected  = Color(0xFF7A9D8C)
private val NavIndicator   = Color(0xFF152B20)

// ---------------------------------------------------------------------------
// AppNavigation
// ---------------------------------------------------------------------------

@Composable
fun AppNavigation() {
    var currentTab by remember { mutableStateOf(AppDestination.Counter) }
    var overlay by remember { mutableStateOf<OverlayScreen>(OverlayScreen.None) }

    val counterViewModel: CounterViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            // Hide bottom bar when showing full-screen overlays
            if (overlay !is OverlayScreen.DuaDetail &&
                overlay !is OverlayScreen.BackgroundPicker &&
                overlay !is OverlayScreen.IslamicTools &&
                overlay !is OverlayScreen.Premium) {
                val strings = LocalStrings.current
                NavigationBar(containerColor = NavBackground) {
                    AppDestination.values().forEach { dest ->
                        val label = when (dest) {
                            AppDestination.Counter   -> strings.tasbeh
                            AppDestination.Analytics -> strings.navAnalytics
                            AppDestination.Reminder  -> strings.navReminder
                            AppDestination.Settings  -> strings.settingsTitle
                        }
                        NavigationBarItem(
                            selected = currentTab == dest && overlay is OverlayScreen.None,
                            onClick  = {
                                currentTab = dest
                                overlay = OverlayScreen.None
                            },
                            icon  = { Icon(dest.icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = NavSelected,
                                selectedTextColor   = NavSelected,
                                unselectedIconColor = NavUnselected,
                                unselectedTextColor = NavUnselected,
                                indicatorColor      = NavIndicator
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val ov = overlay) {
                is OverlayScreen.DuaDetail -> {
                    DuaDetailScreen(
                        categoryId    = ov.categoryId,
                        categoryTitle = ov.categoryTitle,
                        onBack        = { overlay = OverlayScreen.DhikrList() }
                    )
                }

                is OverlayScreen.DhikrList -> {
                    DhikrListScreen(
                        onOpenCategory = { id, title ->
                            overlay = OverlayScreen.DuaDetail(id, title)
                        },
                        onBack = {
                            overlay = OverlayScreen.None
                            currentTab = AppDestination.Counter
                        }
                    )
                }

                is OverlayScreen.BackgroundPicker -> {
                    BackgroundPickerScreen(
                        onBack = { overlay = OverlayScreen.None }
                    )
                }

                is OverlayScreen.IslamicTools -> {
                    IslamicToolsScreen(
                        onBack = { overlay = OverlayScreen.None }
                    )
                }

                is OverlayScreen.Premium -> {
                    PremiumScreen(
                        onBack = { overlay = OverlayScreen.None }
                    )
                }

                OverlayScreen.None -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f)) togetherWith
                                    (fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 1.03f))
                        },
                        label = "nav_anim"
                    ) { destination ->
                        when (destination) {
                            AppDestination.Counter  -> CounterScreen(
                                onNavigateToDhikrList = { overlay = OverlayScreen.DhikrList() },
                                viewModel             = counterViewModel
                            )
                            AppDestination.Analytics -> AnalyticsScreen()
                            AppDestination.Reminder  -> ReminderScreen()
                            AppDestination.Settings  -> SettingsScreen(
                                onOpenBackgroundPicker = { overlay = OverlayScreen.BackgroundPicker },
                                onOpenIslamicTools     = { overlay = OverlayScreen.IslamicTools },
                                onOpenPremium          = { overlay = OverlayScreen.Premium }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Legacy alias
@Composable
fun TasbeehNavHost() = AppNavigation()

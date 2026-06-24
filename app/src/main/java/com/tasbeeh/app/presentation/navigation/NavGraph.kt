package com.tasbeeh.app.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.presentation.counter.CounterScreen
import com.tasbeeh.app.presentation.counter.CounterViewModel
import com.tasbeeh.app.presentation.dhikr.DhikrListScreen
import com.tasbeeh.app.presentation.history.HistoryScreen
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.settings.SettingsScreen

sealed class Screen(val route: String, val icon: ImageVector) {
    object Counter : Screen("counter", Icons.Default.RadioButtonUnchecked)
    object DhikrList : Screen("dhikr_list", Icons.Default.FormatListBulleted)
    object History : Screen("history", Icons.Default.History)
    object Settings : Screen("settings", Icons.Default.Settings)
}

@Composable
fun TasbeehNavHost() {
    val strings = LocalStrings.current
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Counter.route) }

    val counterViewModel: CounterViewModel = hiltViewModel()
    val counterUiState by counterViewModel.uiState.collectAsState()

    val navItems = listOf(
        Screen.Counter to strings.navCounter,
        Screen.DhikrList to strings.navDhikrs,
        Screen.History to strings.navHistory,
        Screen.Settings to strings.navSettings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { (screen, label) ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { currentRoute = screen.route },
                        icon = { Icon(screen.icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                (fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f)) togetherWith
                        (fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 1.03f))
            },
            label = "nav",
            modifier = Modifier.padding(paddingValues)
        ) { route ->
            when (route) {
                Screen.Counter.route -> CounterScreen(
                    onNavigateToDhikrList = { currentRoute = Screen.DhikrList.route },
                    viewModel = counterViewModel
                )
                Screen.DhikrList.route -> DhikrListScreen(
                    selectedDhikrId = counterUiState.selectedDhikr?.id,
                    onSelectDhikr = { dhikr ->
                        counterViewModel.onSelectDhikr(dhikr)
                        currentRoute = Screen.Counter.route
                    }
                )
                Screen.History.route -> HistoryScreen()
                Screen.Settings.route -> SettingsScreen()
            }
        }
    }
}

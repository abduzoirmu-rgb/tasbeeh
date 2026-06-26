package com.tasbeeh.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tasbeeh.app.presentation.localization.LocalStrings

// ---------------------------------------------------------------------------
// Colours
// ---------------------------------------------------------------------------

private val DarkBackground = Color(0xFF0D1B15)
private val DarkSurface    = Color(0xFF112218)
private val PrimaryTeal    = Color(0xFF1D9A6C)
private val OnDarkMuted    = Color(0xFF7A9D8C)
private val OrangeAccent   = Color(0xFFFF9800)

// ---------------------------------------------------------------------------
// SettingsSwitchItem
// ---------------------------------------------------------------------------

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = PrimaryTeal,
            modifier           = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text       = description,
                color      = OnDarkMuted,
                fontSize   = 12.sp,
                lineHeight = 16.sp
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = PrimaryTeal,
                uncheckedThumbColor = OnDarkMuted,
                uncheckedTrackColor = DarkBackground
            )
        )
    }
}

// ---------------------------------------------------------------------------
// SupportItem
// ---------------------------------------------------------------------------

@Composable
fun SupportItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconBg,
                modifier           = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text     = subtitle,
                color    = OnDarkMuted,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = OnDarkMuted,
            modifier           = Modifier.size(20.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// SettingsScreen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenBackgroundPicker: () -> Unit = {},
    onOpenIslamicTools: () -> Unit = {},
    onOpenPremium: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val strings  = LocalStrings.current
    var showLangDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        "ru" to strings.langRu,
        "en" to strings.langEn,
        "tg" to strings.langTj,
        "ar" to strings.langAr
    )
    val currentLangName = languages.firstOrNull { it.first == settings.language }?.second ?: strings.langRu

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            containerColor   = DarkSurface,
            title = {
                Text(text = strings.languageGroup, color = Color.White, fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLanguage(code)
                                    showLangDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.language == code,
                                onClick  = {
                                    viewModel.updateLanguage(code)
                                    showLangDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor   = PrimaryTeal,
                                    unselectedColor = OnDarkMuted
                                )
                            )
                            Text(
                                text     = name,
                                color    = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text(strings.close, color = PrimaryTeal)
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = strings.settingsTitle,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // ----------------------------------------------------------------
            // Toggles section
            // ----------------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                ) {
                    SettingsSwitchItem(
                        title           = strings.autoCountTitle,
                        description     = strings.autoCountSubtitle,
                        icon            = Icons.Default.Timer,
                        checked         = settings.vibrationEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(vibrationEnabled = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.textOnScreenTitle,
                        description     = strings.textOnScreenSubtitle,
                        icon            = Icons.Default.TextFields,
                        checked         = settings.showTextOnScreen,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showTextOnScreen = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.privacyModeTitle,
                        description     = strings.privacyModeSubtitle,
                        icon            = Icons.Default.Security,
                        checked         = settings.privacyMode,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(privacyMode = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.smartTouchTitle,
                        description     = strings.smartTouchSubtitle,
                        icon            = Icons.Default.Fingerprint,
                        checked         = settings.smartTouch,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(smartTouch = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.autoRepeatTitle,
                        description     = strings.autoRepeatSubtitle,
                        icon            = Icons.Default.Repeat,
                        checked         = settings.autoRepeat,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoRepeat = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.keepScreenOnTitle,
                        description     = strings.keepScreenOnSubtitle,
                        icon            = Icons.Default.PhoneAndroid,
                        checked         = settings.keepScreenOn,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(keepScreenOn = it)) }
                    )
                    SettingsDivider()
                    SettingsSwitchItem(
                        title           = strings.dailyGoalTitle,
                        description     = strings.dailyGoalSubtitle,
                        icon            = Icons.Default.EmojiEvents,
                        checked         = settings.dailyGoalEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(dailyGoalEnabled = it)) }
                    )
                }
            }

            // ----------------------------------------------------------------
            // Language row
            // ----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .clickable { showLangDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Language,
                        contentDescription = null,
                        tint               = PrimaryTeal,
                        modifier           = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.languageGroup, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = currentLangName,      color = OnDarkMuted, fontSize = 12.sp)
                    }
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = OnDarkMuted,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ----------------------------------------------------------------
            // Islamic tools row
            // ----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .clickable { onOpenIslamicTools() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint               = PrimaryTeal,
                        modifier           = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.islamicToolsTitle,    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = strings.islamicToolsSubtitle, color = OnDarkMuted, fontSize = 12.sp)
                    }
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = OnDarkMuted,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ----------------------------------------------------------------
            // Background theme row
            // ----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .clickable { onOpenBackgroundPicker() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Palette,
                        contentDescription = null,
                        tint               = PrimaryTeal,
                        modifier           = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.backgroundThemeTitle,    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = strings.backgroundThemeSubtitle, color = OnDarkMuted, fontSize = 12.sp)
                    }
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = OnDarkMuted,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ----------------------------------------------------------------
            // Premium row
            // ----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF1A1200), Color(0xFF2A2000))
                            )
                        )
                        .clickable { onOpenPremium() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Star,
                        contentDescription = null,
                        tint               = Color(0xFFD4AF37),
                        modifier           = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = if (settings.isPremiumUser) strings.premiumActive else strings.premiumGet,
                            color      = Color(0xFFD4AF37),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text     = if (settings.isPremiumUser) strings.premiumThanks else strings.premiumSubtitle,
                            color    = OrangeAccent,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = Color(0xFFD4AF37).copy(alpha = 0.6f),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ----------------------------------------------------------------
            // Developer card
            // ----------------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text       = "О разработчике",
                        color      = OnDarkMuted,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Person,
                                contentDescription = null,
                                tint               = PrimaryTeal,
                                modifier           = Modifier.size(30.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text       = "Муродзода Абдузоир Абдукаюм",
                                color      = Color.White,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text     = "Instagram: @murodzoda_z",
                                color    = PrimaryTeal,
                                fontSize = 12.sp
                            )
                            Text(
                                text     = "Тел: +998 88 370-22-11",
                                color    = OnDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------------------
            // Footer
            // ----------------------------------------------------------------
            item {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = strings.privacyAndTerms, color = OnDarkMuted, fontSize = 11.sp)
                    Text(text = strings.appVersion,     color = OnDarkMuted, fontSize = 11.sp)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkBackground.copy(alpha = 0.6f))
    )
}

package com.tasbeeh.app.presentation.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasbeeh.app.domain.model.PredefinedTasbihTypes
import com.tasbeeh.app.presentation.localization.LocalStrings
import com.tasbeeh.app.presentation.localization.localizedName

private val SheetBg      = Color(0xFF0D1B15)
private val SheetSurface = Color(0xFF112218)
private val TealPrimary  = Color(0xFF1D9A6C)
private val Muted        = Color(0xFF7A9D8C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihTypePickerSheet(
    currentTarget: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val strings = LocalStrings.current
    var showCustomField by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = SheetBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text       = strings.selectCount,
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(bottom = 16.dp)
            )

            PredefinedTasbihTypes.forEach { type ->
                val isActive = !type.isCustom && type.count == currentTarget
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (type.isCustom) {
                                showCustomField = true
                            } else {
                                onSelect(type.count)
                                onDismiss()
                            }
                        }
                        .background(if (isActive) TealPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = if (type.isCustom) Icons.Default.Edit else Icons.Default.Check,
                        contentDescription = null,
                        tint               = if (isActive) TealPrimary else Muted,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text       = type.localizedName(strings),
                        color      = if (isActive) TealPrimary else Color.White,
                        fontSize   = 15.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        modifier   = Modifier.weight(1f)
                    )
                    if (isActive) {
                        Text(
                            text     = "✓",
                            color    = TealPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showCustomField) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value         = customText,
                    onValueChange = { if (it.length <= 5) customText = it.filter(Char::isDigit) },
                    label         = { Text(strings.enterNumber, color = Muted) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val n = customText.toIntOrNull()
                            if (n != null && n in 1..9999) {
                                onSelect(n)
                                onDismiss()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = TealPrimary,
                        unfocusedBorderColor = Muted,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = TealPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = {
                        val n = customText.toIntOrNull()
                        if (n != null && n in 1..9999) {
                            onSelect(n)
                            onDismiss()
                        }
                    }
                ) {
                    Text(strings.apply, color = TealPrimary)
                }
            }
        }
    }
}

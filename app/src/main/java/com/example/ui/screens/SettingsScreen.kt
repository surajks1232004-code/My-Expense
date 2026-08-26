package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CurrencyType
import com.example.data.model.UserProfile
import com.example.data.repository.FinanceState
import com.example.ui.components.GlassCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianElevated
import java.util.Locale

@Composable
fun SettingsScreen(
    state: FinanceState,
    onUpdateProfile: (UserProfile) -> Unit,
    onUpdateCurrency: (CurrencyType) -> Unit,
    onUpdateLanguage: (AppLanguage) -> Unit,
    onUpdateCountry: (String) -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onSyncRealSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = state.settings
    val profile = settings.userProfile

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Profile Card Header (Google Pay / Modern Fintech Style)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ObsidianElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(EmeraldPrimary, ElectricCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.profileEmoji,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile.email,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Monthly Income: ${settings.currency.symbol}${String.format(Locale.US, "%,.0f", profile.monthlyIncome)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ObsidianCard)
                            .clickable { showEditProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Preferences & Currency
        item {
            SettingsSectionHeader(title = "PREFERENCES & LOCALIZATION")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.CurrencyExchange,
                        iconTint = EmeraldPrimary,
                        title = "Currency",
                        subtitle = "${settings.currency.symbol} (${settings.currency.displayName})",
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Public,
                        iconTint = ElectricCyan,
                        title = "Country / Region",
                        subtitle = settings.country,
                        onClick = { showCountryDialog = true }
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        iconTint = Color(0xFFB388FF),
                        title = "Language",
                        subtitle = "${settings.language.displayName} (${settings.language.localizedName})",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
        }

        // Section 2: Security & Privacy
        item {
            SettingsSectionHeader(title = "SECURITY & PRIVACY")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // App Lock Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "App Lock",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Biometric App Lock",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (settings.isAppLockEnabled) "Protected with fingerprint/PIN" else "Disabled",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = settings.isAppLockEnabled,
                            onCheckedChange = { onToggleAppLock(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = ObsidianElevated
                            )
                        )
                    }

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Default.Shield,
                        iconTint = EmeraldPrimary,
                        title = "100% On-Device Privacy",
                        subtitle = "Zero cloud uploads • OTPs & 2FA auto-discarded",
                        showChevron = false,
                        onClick = {}
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        iconTint = ElectricCyan,
                        title = "Storage Encryption",
                        subtitle = "Local Room DB with AES-256 state security",
                        showChevron = false,
                        onClick = {}
                    )
                }
            }
        }

        // Section 3: SMS Intelligence & Data
        item {
            SettingsSectionHeader(title = "DATA & SMS SYNC")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Sync,
                        iconTint = EmeraldPrimary,
                        title = "Scan Device SMS Inbox",
                        subtitle = "Auto-discovers bank & UPI transactions",
                        onClick = onSyncRealSms
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Sms,
                        iconTint = ElectricCyan,
                        title = "Processed SMS Records",
                        subtitle = "${state.lastSmsScanStats?.totalSmsScanned ?: state.transactions.size} records indexed",
                        showChevron = false,
                        onClick = {}
                    )
                }
            }
        }

        // Section 4: About & Build
        item {
            SettingsSectionHeader(title = "ABOUT MY EXPENSE")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Version",
                        subtitle = "v2.4.0-Production (On-Device Intelligence)",
                        showChevron = false,
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        iconTint = EmeraldPrimary,
                        title = "Zero-Knowledge Architecture",
                        subtitle = "Your financial data never leaves this phone",
                        showChevron = false,
                        onClick = {}
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Dialog 1: Edit Profile
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.name) }
        var editEmail by remember { mutableStateOf(profile.email) }
        var editPhone by remember { mutableStateOf(profile.phone) }
        var editIncome by remember { mutableStateOf(profile.monthlyIncome.toString()) }
        var editEmoji by remember { mutableStateOf(profile.profileEmoji) }

        val emojiOptions = listOf("👤", "💼", "🚀", "💎", "🦁", "⚡", "🌟", "🛡️")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = ObsidianCard,
            title = {
                Text("Edit Profile", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Emoji Selector
                    Text("Avatar Icon:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        emojiOptions.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (editEmoji == emoji) EmeraldPrimary.copy(alpha = 0.3f) else ObsidianElevated)
                                    .border(if (editEmoji == emoji) 1.5.dp else 0.dp, EmeraldPrimary, CircleShape)
                                    .clickable { editEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    OutlinedTextField(
                        value = editIncome,
                        onValueChange = { editIncome = it },
                        label = { Text("Monthly Income (${settings.currency.symbol})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val incomeVal = editIncome.toDoubleOrNull() ?: profile.monthlyIncome
                        onUpdateProfile(
                            profile.copy(
                                name = editName.ifBlank { profile.name },
                                email = editEmail.ifBlank { profile.email },
                                phone = editPhone,
                                monthlyIncome = incomeVal,
                                profileEmoji = editEmoji
                            )
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Dialog 2: Currency Picker
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = ObsidianCard,
            title = {
                Text("Select Default Currency", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CurrencyType.entries.forEach { curr ->
                        val isSelected = settings.currency == curr
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    onUpdateCurrency(curr)
                                    showCurrencyDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = curr.symbol,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.width(42.dp)
                                )
                                Column {
                                    Text(
                                        text = curr.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdateCurrency(curr)
                                    showCurrencyDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = EmeraldPrimary)
                }
            }
        )
    }

    // Dialog 3: Country Picker
    if (showCountryDialog) {
        val countries = listOf("India", "United States", "United Kingdom", "United Arab Emirates", "Canada", "Australia", "Singapore", "Germany", "Japan")
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            containerColor = ObsidianCard,
            title = {
                Text("Select Region", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    countries.forEach { c ->
                        val isSelected = settings.country == c
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    onUpdateCountry(c)
                                    showCountryDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = c,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdateCountry(c)
                                    showCountryDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryDialog = false }) {
                    Text("Close", color = EmeraldPrimary)
                }
            }
        )
    }

    // Dialog 4: Language Picker
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = ObsidianCard,
            title = {
                Text("Select Language", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = settings.language == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    onUpdateLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.localizedName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onUpdateLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = EmeraldPrimary)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(ObsidianCardBorder.copy(alpha = 0.4f))
    )
}

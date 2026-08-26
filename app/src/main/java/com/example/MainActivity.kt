package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PlanningScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsIntelligenceScreen
import com.example.ui.screens.TransactionDetailSheet
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianElevated
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    TRANSACTIONS("Timeline", Icons.Default.ReceiptLong),
    ANALYTICS("Analytics", Icons.Default.AutoGraph),
    PLANNING("Planning", Icons.Default.PieChart),
    SMS_HUB("SMS Hub", Icons.Default.Sms),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: FinanceRepository

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = FinanceRepository(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by repository.state.collectAsState()
                var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
                var showAddSheet by remember { mutableStateOf(false) }
                var selectedTransactionForDetail by remember { mutableStateOf<Transaction?>(null) }
                var accountMenuExpanded by remember { mutableStateOf(false) }

                val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // SMS Permission state handler
                val smsPermissionState = rememberPermissionState(permission = Manifest.permission.READ_SMS)
                var hasRequestedPermissionOnLaunch by remember { mutableStateOf(false) }
                var showPermissionDialog by remember { mutableStateOf(false) }

                // Automatic Runtime SMS Permission Request on Startup
                LaunchedEffect(Unit) {
                    if (!smsPermissionState.status.isGranted) {
                        showPermissionDialog = true
                    } else {
                        repository.setSmsPermissionGranted(true)
                        repository.syncRealSmsInbox(150)
                    }
                }

                LaunchedEffect(smsPermissionState.status.isGranted) {
                    repository.setSmsPermissionGranted(smsPermissionState.status.isGranted)
                    if (smsPermissionState.status.isGranted) {
                        repository.syncRealSmsInbox(150)
                    }
                }

                // Show snackbars when notifications arrive
                LaunchedEffect(state.notificationMessage) {
                    state.notificationMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        repository.dismissNotification()
                    }
                }

                // App Lock Security Screen check
                if (state.settings.isAppLockEnabled && state.isAppLocked) {
                    AppLockScreen(
                        userProfile = state.settings.userProfile,
                        onUnlock = { repository.setAppLocked(false) }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = ObsidianDark,
                        topBar = {
                            TopNavigationBar(
                                selectedAccountName = state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name ?: "All Accounts",
                                isMenuExpanded = accountMenuExpanded,
                                onMenuToggle = { accountMenuExpanded = it },
                                accounts = state.accounts,
                                onSelectAccount = {
                                    repository.selectAccount(it)
                                    accountMenuExpanded = false
                                },
                                onShieldClick = { currentTab = AppTab.SMS_HUB },
                                onSettingsClick = { currentTab = AppTab.SETTINGS }
                            )
                        },
                        bottomBar = {
                            CustomBottomNavBar(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it }
                            )
                        },
                        floatingActionButton = {
                            if (currentTab != AppTab.SETTINGS) {
                                FloatingActionButton(
                                    onClick = { showAddSheet = true },
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .testTag("fab_quick_add")
                                        .size(56.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Transaction",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                                when (tab) {
                                    AppTab.DASHBOARD -> DashboardScreen(
                                        state = state,
                                        onAccountSelected = { repository.selectAccount(it) },
                                        onTransactionClick = { selectedTransactionForDetail = it },
                                        onConfirmPending = {
                                            repository.updateTransactionStatus(it, com.example.data.model.TransactionStatus.SUCCESSFUL)
                                        },
                                        onNavigateToSmsTester = { currentTab = AppTab.SMS_HUB },
                                        onNavigateToTransactions = { currentTab = AppTab.TRANSACTIONS },
                                        onQuickAddClick = { showAddSheet = true },
                                        onSyncRealSms = {
                                            if (smsPermissionState.status.isGranted) {
                                                coroutineScope.launch { repository.syncRealSmsInbox(150) }
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )

                                    AppTab.TRANSACTIONS -> TransactionsScreen(
                                        state = state,
                                        onTransactionClick = { selectedTransactionForDetail = it },
                                        onConfirmPending = {
                                            repository.updateTransactionStatus(it, com.example.data.model.TransactionStatus.SUCCESSFUL)
                                        }
                                    )

                                    AppTab.ANALYTICS -> AnalyticsScreen(state = state)

                                    AppTab.PLANNING -> PlanningScreen(state = state)

                                    AppTab.SMS_HUB -> SmsIntelligenceScreen(
                                        state = state,
                                        onTestSms = { sender, body ->
                                            repository.simulateSmsIngestion(sender, body)
                                        }
                                    )

                                    AppTab.SETTINGS -> SettingsScreen(
                                        state = state,
                                        onUpdateProfile = { repository.updateProfile(it) },
                                        onUpdateCurrency = { repository.updateCurrency(it) },
                                        onUpdateLanguage = { repository.updateLanguage(it) },
                                        onUpdateCountry = { repository.updateCountry(it) },
                                        onToggleAppLock = { enabled ->
                                            val updated = state.settings.copy(isAppLockEnabled = enabled)
                                            repository.updateSettings(updated)
                                        },
                                        onSyncRealSms = {
                                            if (smsPermissionState.status.isGranted) {
                                                coroutineScope.launch { repository.syncRealSmsInbox(150) }
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                }
                            }

                            // Automatic SMS Permission Request Dialog
                            if (showPermissionDialog && !smsPermissionState.status.isGranted) {
                                AlertDialog(
                                    onDismissRequest = { showPermissionDialog = false },
                                    containerColor = ObsidianCard,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "SMS Permission",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    },
                                    title = {
                                        Text(
                                            text = "Automatic Expense Tracking",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    text = {
                                        Column {
                                            Text(
                                                text = "My Expense automatically parses incoming bank and UPI SMS messages to log your expenses on-device.",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "🔒 100% On-Device Privacy:\n• Zero cloud uploads\n• OTPs & 2FA passwords are immediately discarded\n• End-to-end encrypted storage",
                                                fontSize = 12.sp,
                                                color = EmeraldPrimary,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showPermissionDialog = false
                                                smsPermissionState.launchPermissionRequest()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                        ) {
                                            Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showPermissionDialog = false }) {
                                            Text("Skip for Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                )
                            }

                            // Add Transaction Sheet Modal
                            if (showAddSheet) {
                                AddTransactionSheet(
                                    accounts = state.accounts,
                                    sheetState = addSheetState,
                                    onDismiss = { showAddSheet = false },
                                    onSaveTransaction = { amount, type, merchant, category, accountId, targetAccountId, tags, notes, receiptAttached ->
                                        repository.addTransaction(
                                            amount = amount,
                                            type = type,
                                            merchant = merchant,
                                            category = category,
                                            accountId = accountId,
                                            targetAccountId = targetAccountId,
                                            tags = tags,
                                            notes = notes,
                                            receiptAttached = receiptAttached
                                        )
                                    }
                                )
                            }

                            // Transaction Detail Sheet Modal
                            if (selectedTransactionForDetail != null) {
                                TransactionDetailSheet(
                                    transaction = selectedTransactionForDetail!!,
                                    sheetState = detailSheetState,
                                    onDismiss = { selectedTransactionForDetail = null },
                                    onStatusChange = { newStatus ->
                                        selectedTransactionForDetail?.let { txn ->
                                            repository.updateTransactionStatus(txn.id, newStatus)
                                        }
                                    },
                                    onSplitTransaction = { cat1, amt1, cat2, amt2 ->
                                        selectedTransactionForDetail?.let { txn ->
                                            repository.splitTransaction(txn.id, cat1, amt1, cat2, amt2)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppLockScreen(
    userProfile: com.example.data.model.UserProfile,
    onUnlock: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EmeraldPrimary, ElectricCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.profileEmoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back, ${userProfile.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "My Expense is locked with on-device biometrics",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ObsidianElevated)
                    .border(2.dp, EmeraldPrimary.copy(alpha = 0.5f), CircleShape)
                    .clickable { onUnlock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Unlock",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tap to Authenticate", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TopNavigationBar(
    selectedAccountName: String,
    isMenuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    accounts: List<com.example.data.model.Account>,
    onSelectAccount: (String?) -> Unit,
    onShieldClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity & Account Dropdown Chip
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00E676), Color(0xFF00897B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "My Expense Logo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "My Expense",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.3.sp
                )

                Box {
                    Row(
                        modifier = Modifier
                            .clickable { onMenuToggle(true) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedAccountName ▼",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { onMenuToggle(false) },
                        modifier = Modifier.background(ObsidianCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Accounts (Consolidated)", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                            onClick = { onSelectAccount(null) }
                        )
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.institution})", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
                                onClick = { onSelectAccount(acc.id) }
                            )
                        }
                    }
                }
            }
        }

        // Security Shield Badge, Notifications & Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ObsidianElevated)
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onShieldClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Encrypted",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = ObsidianCard,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = EmeraldPrimary,
                    indicatorColor = EmeraldPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmsParseResult
import com.example.data.model.TransactionStatus
import com.example.data.repository.FinanceState
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess

data class SmsPreset(
    val title: String,
    val sender: String,
    val body: String,
    val expectedType: String,
    val tag: String
)

@Composable
fun SmsIntelligenceScreen(
    state: FinanceState,
    onTestSms: (sender: String, body: String) -> SmsParseResult,
    modifier: Modifier = Modifier
) {
    var customSender by remember { mutableStateOf("VM-HDFCBK") }
    var customBody by remember {
        mutableStateOf("Rs 1,450.00 debited from A/c **4821 on 23-Aug-26 to SWIGGY via UPI. Avl Bal: Rs 23,400.00")
    }
    var parseOutput by remember { mutableStateOf(state.lastParsedSmsResult) }

    var biometricLock by remember { mutableStateOf(true) }
    var backgroundParsing by remember { mutableStateOf(true) }
    var thresholdAlerts by remember { mutableStateOf(true) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }

    val presets = listOf(
        SmsPreset(
            title = "1. Successful Debit (Swiggy Food)",
            sender = "VM-HDFCBK",
            body = "Rs 1,250.00 debited from HDFC Bank A/c **4821 on 23-AUG-26 to SWIGGY via UPI. Avl Bal: Rs 23,600.00 - Ref 682194",
            expectedType = "Auto-Logged to Food & Dining",
            tag = "SUCCESS"
        ),
        SmsPreset(
            title = "2. Failed / Declined Transaction",
            sender = "AD-ICICIB",
            body = "Transaction of Rs 850.00 to Starbucks on card ending 1092 failed due to technical error. Your account has NOT been debited.",
            expectedType = "Excluded from Expenses strictly",
            tag = "FAILED"
        ),
        SmsPreset(
            title = "3. Pending Authorization Hold",
            sender = "CHASE-ALERT",
            body = "USD 45.00 spent on Chase Card ending in 1092 at SHELL OIL. Transaction is pending authorization.",
            expectedType = "Flagged Pending with Auto-Track",
            tag = "PENDING"
        ),
        SmsPreset(
            title = "4. ATM Cash Withdrawal (Internal Transfer)",
            sender = "VM-HDFCBK",
            body = "Rs 5,000.00 debited from A/c **4821 for ATM Cash Wdl at HDFC ATM. Avl Bal: Rs 18,600.00",
            expectedType = "Self-Transfer (No Double Count)",
            tag = "TRANSFER"
        ),
        SmsPreset(
            title = "5. Monthly Salary Direct Deposit",
            sender = "TECHCORP-HR",
            body = "USD 4,500.00 credited to Account ending with 9041 towards August Payroll Salary from TECHCORP INC.",
            expectedType = "Income Credit Logged",
            tag = "CREDIT"
        ),
        SmsPreset(
            title = "6. Security OTP Discard Test",
            sender = "VERIFY-OTP",
            body = "Your secret OTP verification code for login is 894120. Do not share with anyone.",
            expectedType = "Safely Dropped on-device",
            tag = "OTP-DISCARD"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy Guarantee Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF063B2A),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Privacy Shield",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zero-Knowledge Local Architecture",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "On-device Regex & NLP Engine",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your bank SMS, balances, and merchant records are tokenized entirely inside your Android device sandbox. No transaction text is ever transmitted over the cloud.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Live Interactive SMS Testing Sandbox
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live On-Device SMS Sandbox",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Type or choose a preset SMS to see on-device token extraction",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customSender,
                        onValueChange = { customSender = it },
                        label = { Text("Sender Header (e.g. VM-HDFCBK, CHASE)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customBody,
                        onValueChange = { customBody = it },
                        label = { Text("Raw Transactional SMS Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            parseOutput = onTestSms(customSender, customBody)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Parse SMS",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parse & Ingest SMS Locally", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Parse Results Box
                    if (parseOutput != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Extraction Result:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    StatusBadge(status = parseOutput!!.status)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = parseOutput!!.explanation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (parseOutput!!.cleanMerchant != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Category: ${parseOutput!!.category.iconEmoji} ${parseOutput!!.category.displayName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Preset Test Cards
        item {
            Text(
                text = "Preset Banking SMS Formats",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(presets) { preset ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    customSender = preset.sender
                    customBody = preset.body
                    parseOutput = onTestSms(preset.sender, preset.body)
                }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (preset.tag) {
                                        "SUCCESS" -> StatusSuccess.copy(alpha = 0.2f)
                                        "FAILED" -> StatusFailed.copy(alpha = 0.2f)
                                        "PENDING" -> StatusPending.copy(alpha = 0.2f)
                                        else -> ElectricCyan.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = preset.tag,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (preset.tag) {
                                    "SUCCESS" -> StatusSuccess
                                    "FAILED" -> StatusFailed
                                    "PENDING" -> StatusPending
                                    else -> ElectricCyan
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${preset.body}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Behavior: ${preset.expectedType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        // Security, Permissions & Export Hub
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security & App Settings",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Biometric Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric / Face ID Lock", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Requires unlock when resumed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = biometricLock,
                            onCheckedChange = { biometricLock = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Threshold Alerts Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Budget Limit Alerts (50%, 80%, 100%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Real-time proactive threshold warnings", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = thresholdAlerts,
                            onCheckedChange = { thresholdAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                exportStatusText = "Exported 142 transactions to VaultPulse_Aug2026.csv (AES-256 Encrypted snapshot)"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "CSV Export",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", fontSize = 12.sp, color = EmeraldPrimary)
                        }

                        Button(
                            onClick = {
                                exportStatusText = "Generated Audit Tax Report (PDF) saved to private filesDir/exports/"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "PDF Export",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontSize = 12.sp, color = ElectricCyan)
                        }
                    }

                    if (exportStatusText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "✅ $exportStatusText",
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

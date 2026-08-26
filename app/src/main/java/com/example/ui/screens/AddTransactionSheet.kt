package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.TransactionType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusExpense
import com.example.ui.theme.StatusIncome
import com.example.ui.theme.StatusTransfer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    accounts: List<Account>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        amount: Double,
        type: TransactionType,
        merchant: String,
        category: Category,
        accountId: String,
        targetAccountId: String?,
        tags: List<String>,
        notes: String?,
        receiptAttached: Boolean
    ) -> Unit
) {
    var amountString by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEBIT) }
    var merchantName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.FOOD_DINING) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var targetAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id) }
    var selectedTags by remember { mutableStateOf(listOf("#General")) }
    var customNotes by remember { mutableStateOf("") }
    var isReceiptAttached by remember { mutableStateOf(false) }

    val presetTags = listOf("#Lunch", "#Groceries", "#Vacation2026", "#WorkTrip", "#Coffee", "#Utilities")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Transaction Entry",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Type Toggle Tabs: Expense / Income / Transfer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val activeColor = when (type) {
                        TransactionType.DEBIT -> StatusExpense
                        TransactionType.CREDIT -> StatusIncome
                        TransactionType.TRANSFER -> StatusTransfer
                    }
                    val label = when (type) {
                        TransactionType.DEBIT -> "💸 Expense"
                        TransactionType.CREDIT -> "💼 Income"
                        TransactionType.TRANSFER -> "🔄 Transfer"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input Field with Large Typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = amountString,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            amountString = input
                        }
                    },
                    placeholder = { Text("0.00", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Merchant / Description
            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text(if (selectedType == TransactionType.TRANSFER) "Transfer Description" else "Merchant / Payee Name") },
                placeholder = { Text("e.g. Sweetgreen, Walmart, Apple") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Pickers
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedType == TransactionType.TRANSFER) "Source Account (From):" else "Account:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else ObsidianElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) EmeraldPrimary else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = acc.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Target Account if Transfer
            if (selectedType == TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Destination Account (To):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                            val isSelected = targetAccountId == acc.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) StatusTransfer.copy(alpha = 0.2f) else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) StatusTransfer else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { targetAccountId = acc.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = acc.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) StatusTransfer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category Selection (if not transfer)
            if (selectedType != TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Category.entries.filter { it != Category.TRANSFER }.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(cat.colorHex).copy(alpha = 0.25f) else ObsidianElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(cat.colorHex) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(cat.colorHex) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Tags & Receipt
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianElevated)
                    .clickable { isReceiptAttached = !isReceiptAttached }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Attach Receipt",
                        tint = if (isReceiptAttached) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isReceiptAttached) "Receipt Attached (Encrypted Photo)" else "Attach Receipt Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isReceiptAttached) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stored in private encrypted sandbox",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isReceiptAttached) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Attached",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            val isValid = (amountString.toDoubleOrNull() ?: 0.0) > 0.0 && merchantName.isNotBlank()

            Button(
                onClick = {
                    val amount = amountString.toDoubleOrNull() ?: 0.0
                    val finalCategory = if (selectedType == TransactionType.TRANSFER) Category.TRANSFER else selectedCategory
                    onSaveTransaction(
                        amount,
                        selectedType,
                        merchantName.trim(),
                        finalCategory,
                        selectedAccountId,
                        if (selectedType == TransactionType.TRANSFER) targetAccountId else null,
                        selectedTags,
                        customNotes.takeIf { it.isNotBlank() },
                        isReceiptAttached
                    )
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    disabledContainerColor = EmeraldPrimary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Save Transaction",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

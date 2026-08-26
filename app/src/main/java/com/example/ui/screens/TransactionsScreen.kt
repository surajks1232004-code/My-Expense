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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceState
import com.example.ui.components.GlassCard
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusIncome
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusTransfer

enum class TransactionFilterOption(val label: String) {
    ALL("All"),
    EXPENSES("Expenses"),
    INCOME("Income"),
    TRANSFERS("Transfers"),
    PENDING("Pending"),
    FAILED("Failed")
}

@Composable
fun TransactionsScreen(
    state: FinanceState,
    onTransactionClick: (Transaction) -> Unit,
    onConfirmPending: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TransactionFilterOption.ALL) }

    val sym = state.settings.currency.symbol

    // Filter logic
    val filteredList = state.transactions.filter { txn ->
        val matchesAccount = state.selectedAccountId == null || txn.accountId == state.selectedAccountId

        val matchesSearch = searchQuery.isBlank() ||
                txn.merchant.contains(searchQuery, ignoreCase = true) ||
                txn.category.displayName.contains(searchQuery, ignoreCase = true) ||
                txn.accountName.contains(searchQuery, ignoreCase = true) ||
                (txn.notes?.contains(searchQuery, ignoreCase = true) == true) ||
                txn.tags.any { it.contains(searchQuery, ignoreCase = true) }

        val matchesStatusFilter = when (selectedFilter) {
            TransactionFilterOption.ALL -> true
            TransactionFilterOption.PENDING -> txn.status == TransactionStatus.PENDING
            TransactionFilterOption.FAILED -> txn.status == TransactionStatus.FAILED
            TransactionFilterOption.INCOME -> txn.type == TransactionType.CREDIT
            TransactionFilterOption.TRANSFERS -> txn.status == TransactionStatus.INTERNAL_TRANSFER || txn.type == TransactionType.TRANSFER
            TransactionFilterOption.EXPENSES -> txn.type == TransactionType.DEBIT && txn.status == TransactionStatus.SUCCESSFUL
        }

        matchesAccount && matchesSearch && matchesStatusFilter
    }

    // Grouping by Date
    val grouped = filteredList.groupBy { txn ->
        val diff = System.currentTimeMillis() - txn.timestamp
        val dayMillis = 86_400_000L
        when {
            diff < dayMillis -> "Today"
            diff < dayMillis * 2 -> "Yesterday"
            diff < dayMillis * 7 -> "This Week"
            else -> "Earlier"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Modern Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            placeholder = {
                Text(
                    "Search transactions, merchants, tags...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color(0xFF243048).copy(alpha = 0.6f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TransactionFilterOption.entries.forEach { opt ->
                val isSelected = selectedFilter == opt
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = opt },
                    label = {
                        Text(
                            text = opt.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = ObsidianCard,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF243048).copy(alpha = 0.6f),
                        selectedBorderColor = EmeraldPrimary,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching transactions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing filters or search terms.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (dateGroup, txns) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateGroup,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${txns.size} items",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(txns, key = { it.id }) { txn ->
                        TransactionRowItem(
                            transaction = txn,
                            currencySymbol = sym,
                            onClick = { onTransactionClick(txn) },
                            onConfirmPending = { onConfirmPending(txn.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

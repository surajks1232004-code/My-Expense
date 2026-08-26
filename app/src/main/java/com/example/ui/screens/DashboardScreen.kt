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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceState
import com.example.ui.components.AccountCard
import com.example.ui.components.GlassCard
import com.example.ui.components.PrivacySecurityBanner
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusExpense
import com.example.ui.theme.StatusIncome
import com.example.ui.theme.StatusPending
import java.util.Locale

@Composable
fun DashboardScreen(
    state: FinanceState,
    onAccountSelected: (String?) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onConfirmPending: (String) -> Unit,
    onNavigateToSmsTester: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onQuickAddClick: () -> Unit,
    onSyncRealSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sym = state.settings.currency.symbol

    // Calculate live financial KPIs
    val netWorth = state.accounts.sumOf { it.balance }
    val validTransactions = state.transactions.filter {
        state.selectedAccountId == null || it.accountId == state.selectedAccountId
    }

    val totalExpensesThisMonth = validTransactions
        .filter { it.type == TransactionType.DEBIT && it.status == TransactionStatus.SUCCESSFUL }
        .sumOf { it.amount }

    val totalIncomeThisMonth = validTransactions
        .filter { it.type == TransactionType.CREDIT && it.status == TransactionStatus.SUCCESSFUL }
        .sumOf { it.amount }

    val pendingCount = validTransactions.count { it.status == TransactionStatus.PENDING }

    val budgetLimit = state.budget.totalMonthlyLimit
    val budgetProgress = if (budgetLimit > 0) (totalExpensesThisMonth / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f
    val budgetRemaining = (budgetLimit - totalExpensesThisMonth).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(2.dp))
            PrivacySecurityBanner(onClick = onNavigateToSmsTester)
        }

        // Net Balance Hero Card (Google Pay / PhonePe Style)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF111E2E)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.selectedAccountId == null) "TOTAL BALANCE" else "ACCOUNT BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Active",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Live",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$sym${String.format(Locale.US, "%,.2f", netWorth)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Income vs Expense Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Income Metric Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Income",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "+$sym${String.format(Locale.US, "%,.2f", totalIncomeThisMonth)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusIncome
                                )
                            }
                        }

                        // Expense Metric Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Spent",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "-$sym${String.format(Locale.US, "%,.2f", totalExpensesThisMonth)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Grid (Google Pay / Modern Fintech Buttons)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardActionItem(
                    icon = Icons.Default.Sync,
                    label = "Sync SMS",
                    onClick = onSyncRealSms,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionItem(
                    icon = Icons.Default.Add,
                    label = "Add Expense",
                    onClick = onQuickAddClick,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionItem(
                    icon = Icons.Default.Sms,
                    label = "SMS Hub",
                    onClick = onNavigateToSmsTester,
                    modifier = Modifier.weight(1f)
                )
                DashboardActionItem(
                    icon = Icons.Default.ReceiptLong,
                    label = "Timeline",
                    onClick = onNavigateToTransactions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Linked Accounts Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accounts (${state.accounts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (state.accounts.isNotEmpty() && state.selectedAccountId != null) {
                        Text(
                            text = "Show All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary,
                            modifier = Modifier.clickable { onAccountSelected(null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.accounts.isEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSyncRealSms() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scan Device SMS Inbox",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to auto-detect bank accounts and balance from SMS.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.accounts.forEach { acc ->
                            AccountCard(
                                account = acc,
                                isSelected = state.selectedAccountId == acc.id,
                                currencySymbol = sym,
                                onClick = {
                                    if (state.selectedAccountId == acc.id) {
                                        onAccountSelected(null)
                                    } else {
                                        onAccountSelected(acc.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Monthly Budget Tracker Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Budget Tracker",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$sym${String.format(Locale.US, "%,.2f", totalExpensesThisMonth)} spent of $sym${String.format(Locale.US, "%,.0f", budgetLimit)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (budgetProgress > 0.85f) StatusExpense.copy(alpha = 0.2f)
                                    else EmeraldPrimary.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.0f", budgetProgress * 100)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (budgetProgress > 0.85f) StatusExpense else EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { budgetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (budgetProgress > 0.85f) StatusExpense else EmeraldPrimary,
                        trackColor = ObsidianElevated
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily limit: $sym${String.format(Locale.US, "%.0f", budgetRemaining / 30)}/day",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$sym${String.format(Locale.US, "%,.2f", budgetRemaining)} left",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (pendingCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusPending.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$pendingCount Pending",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusPending
                            )
                        }
                    }
                }

                if (validTransactions.isNotEmpty()) {
                    Text(
                        text = "View All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary,
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }
            }
        }

        // Recent Transaction List Items
        if (validTransactions.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Transactions Recorded",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sync device SMS or log your transactions manually.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onSyncRealSms,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Sync SMS Inbox", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(validTransactions.take(6)) { txn ->
                TransactionRowItem(
                    transaction = txn,
                    currencySymbol = sym,
                    onClick = { onTransactionClick(txn) },
                    onConfirmPending = { onConfirmPending(txn.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun DashboardActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianCard)
            .border(1.dp, Color(0xFF243048).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

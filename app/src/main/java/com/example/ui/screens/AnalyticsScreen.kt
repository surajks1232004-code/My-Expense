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
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceState
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategorySpendItem
import com.example.ui.components.DailySpendTrendChart
import com.example.ui.components.GlassCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusExpense
import com.example.ui.theme.StatusIncome
import java.util.Locale

enum class AnalyticsTimeframe {
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Composable
fun AnalyticsScreen(
    state: FinanceState,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(AnalyticsTimeframe.MONTHLY) }

    val validExpenses = state.transactions.filter {
        it.type == TransactionType.DEBIT && it.status == TransactionStatus.SUCCESSFUL
    }

    val totalExpenses = validExpenses.sumOf { it.amount }.coerceAtLeast(1.0)
    val totalIncome = state.transactions.filter {
        it.type == TransactionType.CREDIT && it.status == TransactionStatus.SUCCESSFUL
    }.sumOf { it.amount }

    val savingsRate = if (totalIncome > 0) {
        (((totalIncome - totalExpenses) / totalIncome) * 100).coerceIn(0.0, 100.0)
    } else 0.0

    // Category breakdown
    val categoryMap = validExpenses.groupBy { it.category }
    val categorySpendItems = Category.entries.mapNotNull { cat ->
        val sum = categoryMap[cat]?.sumOf { it.amount } ?: 0.0
        if (sum > 0) {
            CategorySpendItem(
                category = cat,
                totalAmount = sum,
                percentage = ((sum / totalExpenses) * 100).toFloat()
            )
        } else null
    }.sortedByDescending { it.totalAmount }

    // Top merchants
    val topMerchants = validExpenses
        .groupBy { it.merchant }
        .map { (merchant, txns) -> merchant to txns.sumOf { it.amount } }
        .sortedByDescending { it.second }
        .take(5)

    // Daily spending simulation for current month
    val dailyExpenses = (1..31).map { day ->
        val amount = when (day) {
            3 -> 64.20
            7 -> 32.50
            14 -> 140.00
            18 -> 120.00
            21 -> 26.98
            23 -> 85.00
            else -> if (day % 4 == 0) (20..90).random().toDouble() else (5..35).random().toDouble()
        }
        day to amount
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timeframe selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnalyticsTimeframe.entries.forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                            .clickable { selectedTimeframe = tf }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Income vs Expense Summary Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cash Flow & Savings Rate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🚀 ${String.format(Locale.US, "%.1f", savingsRate)}% Saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Inflow", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "+$${String.format(Locale.US, "%,.2f", totalIncome)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusIncome
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Outflow", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "-$${String.format(Locale.US, "%,.2f", totalExpenses)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusExpense
                            )
                        }
                    }
                }
            }
        }

        // Category Spending Donut Chart Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spending by Category",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap a slice or legend item to filter details",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryDonutChart(
                        items = categorySpendItems,
                        totalSpent = totalExpenses
                    )
                }
            }
        }

        // Daily Burn Rate Graph Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DailySpendTrendChart(
                        dailyExpenses = dailyExpenses,
                        dailyBudgetBenchmark = 112.0
                    )
                }
            }
        }

        // Top Merchants Leaderboard Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top Spending Merchants",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    topMerchants.forEachIndexed { index, (merchant, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = merchant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", amount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

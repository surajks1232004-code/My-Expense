package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusExpense
import com.example.ui.theme.StatusIncome
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class CategorySpendItem(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

@Composable
fun CategoryDonutChart(
    items: List<CategorySpendItem>,
    totalSpent: Double,
    modifier: Modifier = Modifier,
    onCategorySelected: ((Category?) -> Unit)? = null
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .pointerInput(items) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            var currentStartAngle = -90f
                            var found: Category? = null
                            for (item in items) {
                                val sweep = (item.percentage / 100f) * 360f
                                val normalizedAngle = (angle + 90f) % 360f
                                val startNorm = (currentStartAngle + 90f) % 360f
                                if (normalizedAngle >= startNorm && normalizedAngle < startNorm + sweep) {
                                    found = item.category
                                    break
                                }
                                currentStartAngle += sweep
                            }
                            selectedCategory = if (selectedCategory == found) null else found
                            onCategorySelected?.invoke(selectedCategory)
                        }
                    }
            ) {
                val strokeWidth = 26.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background track
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                val totalProgress = animationProgress.value

                for (item in items) {
                    val sweepAngle = (item.percentage / 100f) * 360f * totalProgress
                    val isSelected = selectedCategory == item.category
                    val itemColor = Color(item.category.colorHex)

                    drawArc(
                        color = if (selectedCategory != null && !isSelected) itemColor.copy(alpha = 0.3f) else itemColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(
                            width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += (item.percentage / 100f) * 360f
                }
            }

            // Center Callout Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedCategory != null) {
                    val selectedItem = items.firstOrNull { it.category == selectedCategory }
                    Text(
                        text = selectedCategory?.iconEmoji ?: "",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.0f", selectedItem?.totalAmount ?: 0.0)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(selectedCategory!!.colorHex)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", selectedItem?.percentage ?: 0f)}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%,.0f", totalSpent)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aug 2026",
                        fontSize = 10.sp,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(5).forEach { item ->
                val isSelected = selectedCategory == item.category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ObsidianElevated else Color.Transparent)
                        .clickable {
                            selectedCategory = if (isSelected) null else item.category
                            onCategorySelected?.invoke(selectedCategory)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(item.category.colorHex))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.category.iconEmoji} ${item.category.displayName}",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", item.totalAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${String.format(Locale.US, "%.1f", item.percentage)}%)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailySpendTrendChart(
    dailyExpenses: List<Pair<Int, Double>>, // DayOfMonth to Amount
    dailyBudgetBenchmark: Double = 115.0,
    modifier: Modifier = Modifier
) {
    val maxDaily = (dailyExpenses.maxOfOrNull { it.second } ?: 150.0).coerceAtLeast(180.0)
    var selectedDay by remember { mutableStateOf<Pair<Int, Double>?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Burn Rate Velocity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Daily budget ceiling: $${String.format(Locale.US, "%.0f", dailyBudgetBenchmark)}/day",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedDay != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Day ${selectedDay!!.first}: $${String.format(Locale.US, "%.2f", selectedDay!!.second)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(dailyExpenses) {
                        detectTapGestures { offset ->
                            val barWidth = size.width / dailyExpenses.size
                            val index = (offset.x / barWidth).toInt().coerceIn(0, dailyExpenses.size - 1)
                            selectedDay = dailyExpenses[index]
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val count = dailyExpenses.size
                val stepX = width / count

                // Draw budget ceiling dashed line
                val budgetY = height - ((dailyBudgetBenchmark / maxDaily).toFloat() * height)
                drawLine(
                    color = Color(0xFFFFB300).copy(alpha = 0.7f),
                    start = Offset(0f, budgetY),
                    end = Offset(width, budgetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw Bars
                dailyExpenses.forEachIndexed { idx, pair ->
                    val barHeight = ((pair.second / maxDaily).toFloat() * (height - 20f)).coerceAtLeast(4f)
                    val x = idx * stepX + (stepX * 0.2f)
                    val barW = stepX * 0.6f
                    val y = height - barHeight

                    val isAboveBudget = pair.second > dailyBudgetBenchmark
                    val barColor = if (isAboveBudget) StatusExpense else EmeraldPrimary

                    drawRoundRect(
                        color = if (selectedDay?.first == pair.first) Color.White else barColor,
                        topLeft = Offset(x, y),
                        size = Size(barW, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 8", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 15", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 23 (Today)", fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            Text("Day 31", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

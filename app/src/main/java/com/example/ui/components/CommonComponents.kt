package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.StatusExpense
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusIncome
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusTransfer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = ObsidianCardBorder,
    backgroundColor: Color = ObsidianCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        content()
    }
}

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text, icon) = when (status) {
        TransactionStatus.SUCCESSFUL -> Quad(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            "Success",
            Icons.Default.Check
        )
        TransactionStatus.PENDING -> Quad(
            StatusPending.copy(alpha = 0.2f),
            StatusPending,
            "Pending",
            Icons.Default.Warning
        )
        TransactionStatus.FAILED -> Quad(
            StatusFailed.copy(alpha = 0.2f),
            StatusFailed,
            "Failed",
            Icons.Default.Close
        )
        TransactionStatus.INTERNAL_TRANSFER -> Quad(
            StatusTransfer.copy(alpha = 0.2f),
            StatusTransfer,
            "Transfer",
            Icons.Default.SwapHoriz
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun PrivacySecurityBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0B1F17),
                        Color(0xFF0F1B2B)
                    )
                )
            )
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "On-Device SMS Intelligence",
                    color = EmeraldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero cloud uploads • 100% Private & Encrypted",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(EmeraldPrimary.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Protected",
                color = EmeraldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    currencySymbol: String = "₹",
    onClick: () -> Unit,
    onConfirmPending: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isFailed = transaction.status == TransactionStatus.FAILED
    val isPending = transaction.status == TransactionStatus.PENDING
    val isTransfer = transaction.status == TransactionStatus.INTERNAL_TRANSFER

    val amountColor = when {
        isFailed -> MaterialTheme.colorScheme.onSurfaceVariant
        isTransfer -> StatusTransfer
        transaction.type == TransactionType.CREDIT -> StatusIncome
        else -> StatusExpense
    }

    val amountPrefix = when {
        isTransfer -> "⇄ "
        transaction.type == TransactionType.CREDIT -> "+ "
        else -> "- "
    }

    val formattedDate = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(transaction.timestamp))

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Emoji Container
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(transaction.category.colorHex).copy(alpha = 0.15f))
                            .border(1.dp, Color(transaction.category.colorHex).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.category.iconEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = transaction.merchant,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (isFailed) TextDecoration.LineThrough else null
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isFailed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            if (transaction.receiptAttached) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = "Receipt Attached",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Text(
                            text = "${transaction.accountName} • $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Amount & Status Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isTransfer) "$currencySymbol${String.format(Locale.US, "%,.2f", transaction.amount)}"
                               else "$amountPrefix$currencySymbol${String.format(Locale.US, "%,.2f", transaction.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        style = if (isFailed) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    StatusBadge(status = transaction.status)
                }
            }

            // Quick Confirm Action for Pending
            if (isPending && onConfirmPending != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusPending.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Awaiting clearing confirmation",
                        fontSize = 10.sp,
                        color = StatusPending
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusPending)
                            .clickable { onConfirmPending() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    isSelected: Boolean,
    currencySymbol: String = "₹",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCreditCard = account.type == AccountType.CREDIT_CARD
    val cardColor = Color(account.colorHex)

    Box(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        cardColor.copy(alpha = 0.28f),
                        ObsidianCard
                    )
                )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) EmeraldPrimary else ObsidianCardBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.institution,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (account.lastFourDigits != null) "•• ${account.lastFourDigits}" else account.type.displayName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = if (isCreditCard) "Due" else "Balance",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", Math.abs(account.balance))}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCreditCard) StatusExpense else EmeraldPrimary
                )

                if (isCreditCard && account.creditLimit != null) {
                    val utilization = (Math.abs(account.balance) / account.creditLimit) * 100
                    Text(
                        text = "Limit $currencySymbol${String.format(Locale.US, "%,.0f", account.creditLimit)} (${String.format(Locale.US, "%.0f", utilization)}% used)",
                        fontSize = 9.sp,
                        color = if (utilization > 30) StatusPending else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

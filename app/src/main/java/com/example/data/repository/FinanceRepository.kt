package com.example.data.repository

import android.content.Context
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.AppLanguage
import com.example.data.model.AppSettings
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.CurrencyType
import com.example.data.model.RecurringBill
import com.example.data.model.SavingsGoal
import com.example.data.model.SmsParseResult
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.parser.SmsParserEngine
import com.example.data.sms.RealSmsReader
import com.example.data.sms.RealSmsSyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class FinanceState(
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budget: Budget = Budget(
        totalMonthlyLimit = 45000.0,
        categoryBudgets = mapOf(
            Category.FOOD_DINING to 8000.0,
            Category.GROCERIES to 12000.0,
            Category.SHOPPING to 6000.0,
            Category.TRANSPORT to 4000.0,
            Category.BILLS_UTILITIES to 9000.0,
            Category.ENTERTAINMENT to 3000.0,
            Category.HEALTH_FITNESS to 2000.0
        )
    ),
    val recurringBills: List<RecurringBill> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val selectedAccountId: String? = null, // null means "All Accounts"
    val isBiometricEnabled: Boolean = false,
    val isAppLocked: Boolean = false,
    val isSmsPermissionGranted: Boolean = false,
    val lastParsedSmsResult: SmsParseResult? = null,
    val notificationMessage: String? = null,
    val settings: AppSettings = AppSettings(),
    val isScanningSms: Boolean = false,
    val lastSmsScanStats: RealSmsSyncResult? = null
)

class FinanceRepository(private val context: Context? = null) {

    private val _state = MutableStateFlow(createInitialCleanState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    private val smsReader by lazy { context?.let { RealSmsReader(it) } }

    private fun createInitialCleanState(): FinanceState {
        return FinanceState(
            accounts = emptyList(),
            transactions = emptyList(),
            recurringBills = emptyList(),
            savingsGoals = emptyList(),
            settings = AppSettings(
                userProfile = UserProfile(
                    name = "Suraj",
                    email = "suraj@vaultpulse.io",
                    phone = "+91 98765 43210",
                    monthlyIncome = 75000.0,
                    profileEmoji = "⚡"
                ),
                isAppLockEnabled = false,
                currency = CurrencyType.INR,
                country = "India",
                language = AppLanguage.ENGLISH
            )
        )
    }

    fun setSmsPermissionGranted(granted: Boolean) {
        _state.update { it.copy(isSmsPermissionGranted = granted) }
    }

    fun setAppLocked(locked: Boolean) {
        _state.update { it.copy(isAppLocked = locked) }
    }

    fun updateSettings(newSettings: AppSettings) {
        _state.update { current ->
            current.copy(
                settings = newSettings,
                isBiometricEnabled = newSettings.isAppLockEnabled,
                notificationMessage = "Settings updated successfully"
            )
        }
    }

    fun updateProfile(userProfile: UserProfile) {
        _state.update { current ->
            val updatedSettings = current.settings.copy(userProfile = userProfile)
            current.copy(
                settings = updatedSettings,
                notificationMessage = "Profile updated: ${userProfile.name}"
            )
        }
    }

    fun updateCurrency(currency: CurrencyType) {
        _state.update { current ->
            val updatedSettings = current.settings.copy(currency = currency)
            current.copy(
                settings = updatedSettings,
                notificationMessage = "Currency switched to ${currency.displayName}"
            )
        }
    }

    fun updateLanguage(language: AppLanguage) {
        _state.update { current ->
            val updatedSettings = current.settings.copy(language = language)
            current.copy(
                settings = updatedSettings,
                notificationMessage = "Language set to ${language.displayName}"
            )
        }
    }

    fun updateCountry(country: String) {
        _state.update { current ->
            val updatedSettings = current.settings.copy(country = country)
            current.copy(
                settings = updatedSettings,
                notificationMessage = "Country set to $country"
            )
        }
    }

    suspend fun syncRealSmsInbox(limit: Int = 150) {
        val reader = smsReader ?: return
        _state.update { it.copy(isScanningSms = true) }
        try {
            val syncResult = reader.readDeviceSms(limit)
            _state.update { current ->
                // Merge discovered accounts with existing or replace if previously empty
                val existingAccIds = current.accounts.map { it.name }.toSet()
                val mergedAccounts = current.accounts + syncResult.discoveredAccounts.filter { it.name !in existingAccIds }

                // Merge transactions avoiding duplicates by raw SMS body / refNumber
                val existingRefs = current.transactions.mapNotNull { it.refNumber ?: it.rawSms }.toSet()
                val newUniqueTxns = syncResult.parsedTransactions.filter { (it.refNumber ?: it.rawSms) !in existingRefs }
                val mergedTxns = (newUniqueTxns + current.transactions).sortedByDescending { it.timestamp }

                // Calculate updated balances for accounts based on transactions
                val recalculatedAccounts = (if (mergedAccounts.isEmpty() && syncResult.discoveredAccounts.isNotEmpty()) syncResult.discoveredAccounts else mergedAccounts).map { acc ->
                    val accTxns = mergedTxns.filter { it.accountId == acc.id && it.status == TransactionStatus.SUCCESSFUL }
                    if (accTxns.isNotEmpty()) {
                        val credits = accTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                        val debits = accTxns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                        acc.copy(balance = credits - debits)
                    } else {
                        acc
                    }
                }

                current.copy(
                    accounts = recalculatedAccounts,
                    transactions = mergedTxns,
                    isScanningSms = false,
                    lastSmsScanStats = syncResult,
                    notificationMessage = "Scanned ${syncResult.totalSmsScanned} SMS: Found ${syncResult.transactionalSmsFound} transactions (${syncResult.otpCountFiltered} OTPs safely filtered)."
                )
            }
        } catch (e: Exception) {
            _state.update { current ->
                current.copy(
                    isScanningSms = false,
                    notificationMessage = "SMS Scan failed: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun selectAccount(accountId: String?) {
        _state.update { it.copy(selectedAccountId = accountId) }
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        merchant: String,
        category: Category,
        accountId: String,
        targetAccountId: String? = null,
        tags: List<String> = emptyList(),
        notes: String? = null,
        receiptAttached: Boolean = false
    ) {
        val sym = _state.value.settings.currency.symbol
        var currentAccounts = _state.value.accounts
        if (currentAccounts.isEmpty()) {
            val defaultAcc = Account(
                id = "acc-primary",
                name = "Primary Bank",
                type = AccountType.BANK,
                lastFourDigits = "1001",
                institution = "Primary Bank",
                balance = 0.0,
                colorHex = 0xFF00897B
            )
            currentAccounts = listOf(defaultAcc)
        }

        val account = currentAccounts.firstOrNull { it.id == accountId } ?: currentAccounts.first()
        val status = if (type == TransactionType.TRANSFER) TransactionStatus.INTERNAL_TRANSFER else TransactionStatus.SUCCESSFUL

        val newTx = Transaction(
            accountId = account.id,
            accountName = account.name,
            amount = amount,
            type = type,
            status = status,
            rawMerchant = merchant,
            merchant = merchant,
            category = category,
            tags = tags,
            timestamp = System.currentTimeMillis(),
            receiptAttached = receiptAttached,
            notes = notes,
            isManual = true,
            targetAccountId = targetAccountId
        )

        // Update account balances
        val updatedAccounts = currentAccounts.map { acc ->
            when {
                acc.id == account.id -> {
                    val delta = when (type) {
                        TransactionType.DEBIT -> -amount
                        TransactionType.CREDIT -> amount
                        TransactionType.TRANSFER -> -amount
                    }
                    acc.copy(balance = acc.balance + delta)
                }
                targetAccountId != null && acc.id == targetAccountId -> {
                    acc.copy(balance = acc.balance + amount)
                }
                else -> acc
            }
        }

        _state.update { current ->
            current.copy(
                accounts = updatedAccounts,
                transactions = listOf(newTx) + current.transactions,
                notificationMessage = "Logged $merchant: $sym$amount successfully."
            )
        }
    }

    fun updateTransactionStatus(transactionId: String, newStatus: TransactionStatus) {
        _state.update { current ->
            val updatedList = current.transactions.map { txn ->
                if (txn.id == transactionId) txn.copy(status = newStatus) else txn
            }
            current.copy(
                transactions = updatedList,
                notificationMessage = "Transaction status updated to ${newStatus.name}"
            )
        }
    }

    fun splitTransaction(
        transactionId: String,
        firstCategory: Category,
        firstAmount: Double,
        secondCategory: Category,
        secondAmount: Double
    ) {
        val sym = _state.value.settings.currency.symbol
        val existing = _state.value.transactions.firstOrNull { it.id == transactionId } ?: return
        val part1 = existing.copy(
            id = UUID.randomUUID().toString(),
            category = firstCategory,
            amount = firstAmount,
            notes = (existing.notes ?: "") + " [Split Part 1]"
        )
        val part2 = existing.copy(
            id = UUID.randomUUID().toString(),
            category = secondCategory,
            amount = secondAmount,
            notes = (existing.notes ?: "") + " [Split Part 2]"
        )

        _state.update { current ->
            val remaining = current.transactions.filter { it.id != transactionId }
            current.copy(
                transactions = listOf(part1, part2) + remaining,
                notificationMessage = "Transaction split into $firstCategory ($sym$firstAmount) & $secondCategory ($sym$secondAmount)"
            )
        }
    }

    fun simulateSmsIngestion(sender: String, messageText: String): SmsParseResult {
        val sym = _state.value.settings.currency.symbol
        val parseResult = SmsParserEngine.parseSms(sender, messageText)
        if (parseResult.isTransactional && parseResult.amount != null && parseResult.type != null) {
            var accountsList = _state.value.accounts
            if (accountsList.isEmpty()) {
                val autoCreatedAcc = Account(
                    id = "acc-auto-${UUID.randomUUID().toString().take(6)}",
                    name = "${sender.substringAfter("-").takeIf { it.isNotBlank() } ?: "Bank"} Account",
                    type = if (messageText.contains("card", ignoreCase = true)) AccountType.CREDIT_CARD else AccountType.BANK,
                    lastFourDigits = parseResult.accountRef ?: "1001",
                    institution = sender.substringAfter("-").takeIf { it.isNotBlank() } ?: "Bank",
                    balance = parseResult.balanceAfter ?: 0.0,
                    colorHex = 0xFF00897B
                )
                accountsList = listOf(autoCreatedAcc)
            }

            val matchedAccount = accountsList.firstOrNull { acc ->
                parseResult.accountRef != null && acc.lastFourDigits == parseResult.accountRef
            } ?: accountsList.first()

            val newTxn = Transaction(
                accountId = matchedAccount.id,
                accountName = matchedAccount.name,
                amount = parseResult.amount,
                type = parseResult.type,
                status = parseResult.status,
                rawMerchant = parseResult.rawMerchant ?: sender,
                merchant = parseResult.cleanMerchant ?: "Vendor",
                category = parseResult.category,
                tags = listOf("#AutoSMS", "#${parseResult.status.name}"),
                timestamp = System.currentTimeMillis(),
                refNumber = parseResult.refNumber,
                rawSms = messageText,
                notes = "Auto-parsed on-device from $sender"
            )

            // Adjust balance only if SUCCESSFUL or INTERNAL_TRANSFER
            val updatedAccounts = if (parseResult.status == TransactionStatus.SUCCESSFUL) {
                accountsList.map { acc ->
                    if (acc.id == matchedAccount.id) {
                        val delta = if (parseResult.type == TransactionType.DEBIT) -parseResult.amount else parseResult.amount
                        acc.copy(balance = acc.balance + delta)
                    } else acc
                }
            } else accountsList

            _state.update { current ->
                current.copy(
                    accounts = updatedAccounts,
                    transactions = listOf(newTxn) + current.transactions,
                    lastParsedSmsResult = parseResult,
                    notificationMessage = "Parsed SMS from $sender: ${parseResult.status.name} ($sym${parseResult.amount})"
                )
            }
        } else {
            _state.update { current ->
                current.copy(
                    lastParsedSmsResult = parseResult,
                    notificationMessage = parseResult.explanation
                )
            }
        }
        return parseResult
    }

    fun dismissNotification() {
        _state.update { it.copy(notificationMessage = null) }
    }
}

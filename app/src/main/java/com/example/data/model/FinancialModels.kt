package com.example.data.model

import java.util.UUID

enum class TransactionType {
    DEBIT,
    CREDIT,
    TRANSFER
}

enum class TransactionStatus {
    SUCCESSFUL,
    PENDING,
    FAILED,
    INTERNAL_TRANSFER
}

enum class AccountType(val displayName: String) {
    BANK("Bank Account"),
    CREDIT_CARD("Credit Card"),
    CASH_WALLET("Cash Wallet"),
    DIGITAL_WALLET("Digital Wallet")
}

enum class Category(val displayName: String, val iconEmoji: String, val colorHex: Long) {
    FOOD_DINING("Food & Dining", "🍔", 0xFFFF7043),
    GROCERIES("Groceries", "🛒", 0xFF4CAF50),
    SHOPPING("Shopping", "🛍️", 0xFFAB47BC),
    TRANSPORT("Transport & Fuel", "🚗", 0xFF29B6F6),
    BILLS_UTILITIES("Bills & Utilities", "⚡", 0xFFFFCA28),
    ENTERTAINMENT("Entertainment", "🍿", 0xFFEC407A),
    HEALTH_FITNESS("Health & Fitness", "💊", 0xFF26A69A),
    SALARY_INCOME("Salary & Income", "💼", 0xFF66BB6A),
    INVESTMENTS("Investments & Returns", "📈", 0xFF5C6BC0),
    TRANSFER("Internal Transfer", "🔄", 0xFF78909C),
    MISCELLANEOUS("General & Other", "📦", 0xFF8D6E63);

    companion object {
        fun fromName(name: String): Category {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: MISCELLANEOUS
        }
    }
}

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType,
    val lastFourDigits: String? = null,
    val institution: String,
    val balance: Double,
    val creditLimit: Double? = null, // for credit cards
    val paymentDueDay: Int? = null,  // day of month
    val colorHex: Long = 0xFF1E88E5
)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val accountName: String,
    val amount: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val rawMerchant: String,
    val merchant: String,
    val category: Category,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val refNumber: String? = null,
    val rawSms: String? = null,
    val receiptAttached: Boolean = false,
    val notes: String? = null,
    val isManual: Boolean = false,
    val targetAccountId: String? = null
)

data class Budget(
    val totalMonthlyLimit: Double,
    val categoryBudgets: Map<Category, Double>
)

data class RecurringBill(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val dueDayOfMonth: Int,
    val category: Category,
    val isAutoDebited: Boolean,
    val accountName: String,
    val isPaidThisMonth: Boolean = false
)

data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDateStr: String,
    val iconEmoji: String,
    val colorHex: Long
)

data class SmsParseResult(
    val isTransactional: Boolean,
    val amount: Double? = null,
    val type: TransactionType? = null,
    val status: TransactionStatus = TransactionStatus.SUCCESSFUL,
    val rawMerchant: String? = null,
    val cleanMerchant: String? = null,
    val category: Category = Category.MISCELLANEOUS,
    val accountRef: String? = null,
    val balanceAfter: Double? = null,
    val refNumber: String? = null,
    val explanation: String = ""
)

enum class CurrencyType(val code: String, val symbol: String, val displayName: String) {
    INR("INR", "₹", "Indian Rupee (₹)"),
    USD("USD", "$", "US Dollar ($)"),
    EUR("EUR", "€", "Euro (€)"),
    GBP("GBP", "£", "British Pound (£)"),
    AED("AED", "AED ", "UAE Dirham (AED)"),
    CAD("CAD", "C$", "Canadian Dollar (C$)"),
    AUD("AUD", "A$", "Australian Dollar (A$)"),
    SGD("SGD", "S$", "Singapore Dollar (S$)"),
    JPY("JPY", "¥", "Japanese Yen (¥)")
}

enum class AppLanguage(val code: String, val displayName: String, val localizedName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    SPANISH("es", "Spanish", "Español"),
    FRENCH("fr", "French", "Français"),
    GERMAN("de", "German", "Deutsch"),
    ARABIC("ar", "Arabic", "العربية")
}

data class UserProfile(
    val name: String = "Alex Morgan",
    val email: String = "alex.morgan@vaultpulse.io",
    val phone: String = "+1 (555) 389-2041",
    val monthlyIncome: Double = 6500.0,
    val profileEmoji: String = "⚡"
)

data class AppSettings(
    val userProfile: UserProfile = UserProfile(),
    val isAppLockEnabled: Boolean = false,
    val currency: CurrencyType = CurrencyType.INR,
    val country: String = "India",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val autoCategorizeSms: Boolean = true,
    val filterOtpMessages: Boolean = true,
    val hideAccountNumbers: Boolean = true
)

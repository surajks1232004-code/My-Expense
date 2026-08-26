package com.example.data.parser

import com.example.data.model.Category
import com.example.data.model.SmsParseResult
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType

object SmsParserEngine {

    // Sensitive / OTP filters: Immediately drop OTPs
    private val OTP_REGEX = Regex("""(?i)\b(otp|one time password|verification code|secret code|auth code|login pin)\b""")

    // Amount patterns
    private val AMOUNT_PATTERNS = listOf(
        Regex("""(?i)(?:rs\.?|inr|usd|\$|€|£|aed|cad|aud|sgd|jpy|₹)\s*([\d,]+(?:\.\d{1,2})?)"""),
        Regex("""(?i)(?:debited|spent|paid|withdrawn|credited|received|transferred|amount of|txn of|vpa)\s*(?:by|of|for|with)?\s*(?:rs\.?|inr|usd|\$|€|£|aed|cad|aud|sgd|jpy|₹)?\s*([\d,]+(?:\.\d{1,2})?)"""),
        Regex("""(?i)([\d,]+(?:\.\d{1,2})?)\s*(?:debited|credited|spent|paid|sent|withdrawn)""")
    )

    // Status matchers
    private val FAILED_PATTERNS = listOf(
        Regex("""(?i)(?:failed|declined|unsuccessful|rejected|reversed|insufficient funds|declined due to|server error|cancelled|timed out)"""),
        Regex("""(?i)transaction\s*(?:of|for)?\s*.*\s*(?:failed|declined)""")
    )

    private val PENDING_PATTERNS = listOf(
        Regex("""(?i)(?:pending|under processing|submitted for clearing|in progress|authorization hold|awaiting confirmation|processing)""")
    )

    // Debit / Credit keywords
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "withdrawn", "deducted", "charged", "purchase", "sent", "transferred to", "vpa")
    private val CREDIT_KEYWORDS = listOf("credited", "deposited", "refunded", "received", "added", "cashback", "salary", "payroll", "credited to")

    // ATM Cash withdrawal patterns
    private val ATM_PATTERNS = listOf(
        Regex("""(?i)(?:atm\s*wdl|atm\s*cash|cash\s*withdrawal|withdrawn\s*at\s*atm|atm\s*debit)""")
    )

    // Account / Card number patterns
    private val ACCOUNT_PATTERNS = listOf(
        Regex("""(?i)(?:a/c|acct|acc|account|card)\s*(?:no\.?|ending)?\s*(?:ending with|ending in|x+|\*+)?\s*(\d{3,4})"""),
        Regex("""(?i)(?:ending in|ending with)\s*(\d{3,4})"""),
        Regex("""(?i)(?:x+|\*+)(\d{3,4})""")
    )

    // Ref numbers / UPI RRN
    private val REF_PATTERNS = listOf(
        Regex("""(?i)(?:ref\s*no|rrn|txn\s*id|upi\s*ref|reference\s*no|utr)[\s:]*([A-Za-z0-9]+)"""),
        Regex("""(?i)upi/([A-Za-z0-9]+)""")
    )

    // Merchant extraction
    private val MERCHANT_PATTERNS = listOf(
        Regex("""(?i)(?:at|to|info|vpa|towards|merchant)\s+([A-Za-z0-9\.\*\s\-_@]{2,30}?)(?:\s+(?:on|ref|upi|avl|bal|using|via|dated|\.|\n))"""),
        Regex("""(?i)vpa\s+([a-zA-Z0-9.\-_]+@[a-zA-Z0-9]+)"""),
        Regex("""(?i)(?:paid to|spent on)\s+([A-Za-z0-9\s\.\-_]+)""")
    )

    // Category dictionary for auto-categorization
    private val CATEGORY_RULES = mapOf(
        Category.FOOD_DINING to listOf("swiggy", "zomato", "starbucks", "mcdonalds", "uber eats", "dominos", "chipotle", "subway", "kfc", "diner", "cafe", "restaurant", "sweetgreen", "burger"),
        Category.GROCERIES to listOf("walmart", "target", "costco", "trader joe", "whole foods", "blinkit", "instacart", "zepto", "safeway", "supermarket", "grocery", "fresh"),
        Category.SHOPPING to listOf("amazon", "flipkart", "myntra", "zara", "nike", "apple store", "h&m", "ebay", "uniqlo", "best buy", "nordstrom", "asos"),
        Category.TRANSPORT to listOf("uber", "lyft", "ola", "shell", "chevron", "bp", "exxon", "gas station", "fuel", "metro", "subway pass", "transit", "toll", "fastag"),
        Category.BILLS_UTILITIES to listOf("electricity", "power", "water bill", "airtel", "verizon", "at&t", "jio", "broadband", "wifi", "gas corp", "utility", "insurance"),
        Category.ENTERTAINMENT to listOf("netflix", "spotify", "hulu", "disney", "prime video", "movie", "amc", "cinema", "apple music", "youtube premium", "playstation", "steam"),
        Category.HEALTH_FITNESS to listOf("cvs", "walgreens", "pharmacy", "gym", "fitness", "apollo", "doctor", "clinic", "hospital", "equinox"),
        Category.SALARY_INCOME to listOf("salary", "payroll", "stipend", "bonus", "dividend", "interest credited", "consulting fees", "techcorp"),
        Category.INVESTMENTS to listOf("zerodha", "groww", "vanguard", "fidelity", "robinhood", "mutual fund", "sip", "etf", "coinbase")
    )

    fun parseSms(sender: String, messageBody: String): SmsParseResult {
        val trimmed = messageBody.trim()

        // 1. Check if OTP - reject if sensitive
        if (OTP_REGEX.containsMatchIn(trimmed)) {
            return SmsParseResult(
                isTransactional = false,
                explanation = "Ignored: Security OTP message discarded on-device."
            )
        }

        // 2. Extract Amount
        var detectedAmount: Double? = null
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null) {
                val rawVal = match.groupValues[1].replace(",", "")
                detectedAmount = rawVal.toDoubleOrNull()
                if (detectedAmount != null && detectedAmount > 0) break
            }
        }

        if (detectedAmount == null) {
            return SmsParseResult(
                isTransactional = false,
                explanation = "Non-transactional SMS: No valid monetary amount found."
            )
        }

        // 3. Status Classification
        val isFailed = FAILED_PATTERNS.any { it.containsMatchIn(trimmed) }
        val isPending = PENDING_PATTERNS.any { it.containsMatchIn(trimmed) }
        val isAtm = ATM_PATTERNS.any { it.containsMatchIn(trimmed) }

        val status = when {
            isFailed -> TransactionStatus.FAILED
            isPending -> TransactionStatus.PENDING
            isAtm -> TransactionStatus.INTERNAL_TRANSFER
            else -> TransactionStatus.SUCCESSFUL
        }

        // 4. Debit vs Credit
        val lower = trimmed.lowercase()
        val hasDebit = DEBIT_KEYWORDS.any { lower.contains(it) }
        val hasCredit = CREDIT_KEYWORDS.any { lower.contains(it) }

        val type = when {
            isAtm -> TransactionType.TRANSFER
            hasCredit && !hasDebit -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }

        // 5. Account Number
        var accountRef: String? = null
        for (pattern in ACCOUNT_PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null) {
                accountRef = match.groupValues[1]
                break
            }
        }

        // 6. Ref / RRN Number
        var refNumber: String? = null
        for (pattern in REF_PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null) {
                refNumber = match.groupValues[1]
                break
            }
        }

        // 7. Merchant Sanitization
        var rawMerchant: String? = null
        for (pattern in MERCHANT_PATTERNS) {
            val match = pattern.find(trimmed)
            if (match != null) {
                rawMerchant = match.groupValues[1].trim()
                break
            }
        }

        var cleanMerchant = rawMerchant ?: sender.substringAfter("-").takeIf { it.isNotBlank() } ?: "Bank Transaction"
        cleanMerchant = cleanMerchant.replace(Regex("""(?i)vpa|upi|at\s|to\s|dated|avl\s*bal.*"""), "").trim()
        if (cleanMerchant.length > 24) {
            cleanMerchant = cleanMerchant.substring(0, 24).trim()
        }
        if (cleanMerchant.isBlank()) cleanMerchant = "Merchant"

        if (isAtm) {
            cleanMerchant = "ATM Cash Withdrawal"
        }

        // 8. Auto Categorization
        var category = if (type == TransactionType.CREDIT) Category.SALARY_INCOME else Category.MISCELLANEOUS
        if (isAtm) {
            category = Category.TRANSFER
        } else {
            val searchTarget = "$lower $cleanMerchant"
            for ((cat, keywords) in CATEGORY_RULES) {
                if (keywords.any { searchTarget.contains(it) }) {
                    category = cat
                    break
                }
            }
        }

        val explanation = buildString {
            append("Parsed locally: ")
            append(if (type == TransactionType.CREDIT) "Credit +" else "Debit -")
            append("$$detectedAmount | ")
            append(status.name)
            append(" | Merchant: $cleanMerchant")
            if (accountRef != null) append(" | A/c **$accountRef")
        }

        return SmsParseResult(
            isTransactional = true,
            amount = detectedAmount,
            type = type,
            status = status,
            rawMerchant = rawMerchant,
            cleanMerchant = cleanMerchant,
            category = category,
            accountRef = accountRef,
            refNumber = refNumber,
            explanation = explanation
        )
    }
}

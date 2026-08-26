package com.example.data.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.parser.SmsParserEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class RealSmsSyncResult(
    val parsedTransactions: List<Transaction>,
    val discoveredAccounts: List<Account>,
    val totalSmsScanned: Int,
    val transactionalSmsFound: Int,
    val otpCountFiltered: Int
)

class RealSmsReader(private val context: Context) {

    suspend fun readDeviceSms(limit: Int = 100): RealSmsSyncResult = withContext(Dispatchers.IO) {
        val parsedList = mutableListOf<Transaction>()
        val accountsMap = mutableMapOf<String, Account>()
        var scannedCount = 0
        var txnCount = 0
        var otpCount = 0

        try {
            val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $limit"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    scannedCount++
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "SMS" else "SMS"
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    val result = SmsParserEngine.parseSms(address, body)
                    if (result.explanation.contains("OTP", ignoreCase = true)) {
                        otpCount++
                    }

                    if (result.isTransactional && result.amount != null && result.type != null) {
                        txnCount++
                        
                        // Extract bank institution name from SMS Sender (e.g., "VM-HDFCBK" -> "HDFC Bank")
                        val institutionName = extractInstitution(address)
                        val last4 = result.accountRef ?: "0000"
                        val accountKey = "$institutionName-$last4"

                        val account = accountsMap.getOrPut(accountKey) {
                            Account(
                                id = "acc-${UUID.randomUUID().toString().take(8)}",
                                name = "$institutionName Account",
                                type = if (body.contains("card", ignoreCase = true)) AccountType.CREDIT_CARD else AccountType.BANK,
                                lastFourDigits = last4,
                                institution = institutionName,
                                balance = result.balanceAfter ?: 0.0,
                                colorHex = getAccountColor(institutionName)
                            )
                        }

                        val txn = Transaction(
                            id = "sms-${UUID.randomUUID()}",
                            accountId = account.id,
                            accountName = account.name,
                            amount = result.amount,
                            type = result.type,
                            status = result.status,
                            rawMerchant = result.rawMerchant ?: address,
                            merchant = result.cleanMerchant ?: "Merchant",
                            category = result.category,
                            tags = listOf("#RealSMS", "#${result.status.name}"),
                            timestamp = date,
                            refNumber = result.refNumber,
                            rawSms = body,
                            notes = "Imported from device inbox ($address)"
                        )
                        parsedList.add(txn)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RealSmsReader", "Error reading SMS inbox: ${e.message}", e)
        }

        // If transactions had balance info, update account balances, otherwise calculate from net delta
        val finalizedAccounts = accountsMap.values.map { acc ->
            val accountTxns = parsedList.filter { it.accountId == acc.id && it.status == TransactionStatus.SUCCESSFUL }
            if (acc.balance == 0.0 && accountTxns.isNotEmpty()) {
                val totalCredits = accountTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                val totalDebits = accountTxns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                acc.copy(balance = (totalCredits - totalDebits).coerceAtLeast(0.0))
            } else {
                acc
            }
        }

        RealSmsSyncResult(
            parsedTransactions = parsedList,
            discoveredAccounts = finalizedAccounts,
            totalSmsScanned = scannedCount,
            transactionalSmsFound = txnCount,
            otpCountFiltered = otpCount
        )
    }

    private fun extractInstitution(sender: String): String {
        val clean = sender.uppercase()
        return when {
            clean.contains("HDFC") -> "HDFC Bank"
            clean.contains("ICICI") -> "ICICI Bank"
            clean.contains("SBI") || clean.contains("SBIN") -> "State Bank of India"
            clean.contains("AXIS") -> "Axis Bank"
            clean.contains("KOTAK") -> "Kotak Bank"
            clean.contains("CHASE") -> "Chase Bank"
            clean.contains("BOA") || clean.contains("BAC") -> "Bank of America"
            clean.contains("CITI") -> "Citibank"
            clean.contains("PAYTM") || clean.contains("PYTM") -> "Paytm Wallet"
            clean.contains("GPAY") || clean.contains("GOOGLE") -> "Google Pay"
            clean.contains("AMEX") -> "American Express"
            else -> sender.substringAfter("-").takeIf { it.isNotBlank() } ?: "Bank"
        }
    }

    private fun getAccountColor(institution: String): Long {
        return when {
            institution.contains("HDFC") -> 0xFF00897B
            institution.contains("ICICI") -> 0xFFD84315
            institution.contains("SBI") -> 0xFF1976D2
            institution.contains("Axis") -> 0xFF880E4F
            institution.contains("Kotak") -> 0xFFC62828
            institution.contains("Chase") -> 0xFF1565C0
            else -> 0xFF2E7D32
        }
    }
}

package com.pocketledger.app.model

import java.time.Instant
import java.util.UUID
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

enum class EntryType { INCOME, EXPENSE }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class TextSize(val scale: Float) { SMALL(0.88f), STANDARD(1f), LARGE(1.14f), EXTRA_LARGE(1.28f) }
const val DEFAULT_CATEGORY_COLOR = 0xFF718078

data class LedgerEntry(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local",
    val type: EntryType = EntryType.EXPENSE,
    val amount: Double = 0.0,
    val currency: String = "CNY",
    val purpose: String = "",
    val categoryId: String? = null,
    val category: String = "",
    val accountId: String? = null,
    val accountName: String = "",
    val occurredAt: Long = Instant.now().toEpochMilli(),
    val note: String = "",
    val rawText: String = "",
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli(),
)

data class LedgerAccount(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local",
    val name: String = "",
    val type: String = "Cash",
    val initialBalance: Double = 0.0,
    val balance: Double = 0.0,
    val currency: String = "CNY",
    val includeInAssets: Boolean = true,
    val archived: Boolean = false,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli(),
)

data class LedgerCategory(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local",
    val name: String = "",
    val type: EntryType = EntryType.EXPENSE,
    val icon: String = "·",
    val color: Long = DEFAULT_CATEGORY_COLOR,
    val archived: Boolean = false,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli(),
)

data class Budget(
    val total: Double = 5000.0,
    val categoryAmounts: Map<String, Double> = mapOf("Food" to 1200.0, "Transport" to 500.0),
    val currency: String = "CNY",
)

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val textSize: TextSize = TextSize.STANDARD,
    val currency: String = "CNY",
    val language: String = "zh-TW",
)

data class StoredState(
    val entries: List<LedgerEntry> = emptyList(),
    val accounts: List<LedgerAccount> = emptyList(),
    val categories: List<LedgerCategory> = emptyList(),
    val budget: Budget = Budget(),
    val settings: AppSettings = AppSettings(),
)

val expenseCategories = listOf("Food", "Health", "Education", "Transport", "Shopping", "Entertainment", "Housing", "Travel", "Communication", "Subscriptions", "Other")
val incomeCategories = listOf("Salary", "Scholarship", "Refund", "Transfer In", "Investment Income", "Part-time Job", "Other Income")
val currencies = listOf("CNY", "HKD", "USD", "EUR", "GBP", "JPY", "KRW", "SGD", "AUD", "CAD")
val accountTypes = listOf("Cash", "Bank Account", "Credit Card", "E-wallet", "Transport Card", "Investment Account", "Other")

object LedgerConstraints {
    const val MAX_TRANSACTIONS = 100_000
    // Keeps even the maximum supported transaction count within Long minor-unit totals.
    const val MAX_ABSOLUTE_AMOUNT = 10_000_000_000.0
    const val MAX_NAME_LENGTH = 100
    const val MAX_PURPOSE_LENGTH = 200
    const val MAX_NOTE_LENGTH = 4_000
    const val MAX_SENTENCE_LENGTH = 4_000

    fun isValidTransactionAmount(amount: Double): Boolean =
        amount.isFinite() && amount > 0.0 && amount <= MAX_ABSOLUTE_AMOUNT

    fun isValidBalance(amount: Double): Boolean =
        amount.isFinite() && abs(amount) <= MAX_ABSOLUTE_AMOUNT

    fun isValidBudget(amount: Double): Boolean =
        amount.isFinite() && amount in 0.0..MAX_ABSOLUTE_AMOUNT
}

object Money {
    fun fractionDigits(currency: String): Int = if (currency in setOf("JPY", "KRW")) 0 else 2

    fun toMinor(amount: Double, currency: String): Long = BigDecimal.valueOf(amount)
        .movePointRight(fractionDigits(currency))
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()

    fun fromMinor(amountMinor: Long, currency: String): Double = BigDecimal.valueOf(amountMinor)
        .movePointLeft(fractionDigits(currency))
        .toDouble()
}

fun defaultCategories(now: Long = Instant.now().toEpochMilli()): List<LedgerCategory> =
    expenseCategories.map { name -> LedgerCategory(name = name, type = EntryType.EXPENSE, icon = defaultCategoryIcon(name), color = defaultCategoryColorValue(name), createdAt = now, updatedAt = now) } +
        incomeCategories.map { name -> LedgerCategory(name = name, type = EntryType.INCOME, icon = defaultCategoryIcon(name), color = defaultCategoryColorValue(name), createdAt = now, updatedAt = now) }

fun defaultCategoryColorValue(name: String): Long = when (name) {
    "Food" -> 0xFFE99542
    "Health" -> 0xFFE26A78
    "Education" -> 0xFF6B7FD7
    "Transport" -> 0xFF3B9BA5
    "Shopping" -> 0xFFAC6AC7
    "Entertainment" -> 0xFFCB5F95
    "Housing" -> 0xFF8D7456
    "Travel" -> 0xFF4B8ACA
    "Communication" -> 0xFF5A8F80
    "Subscriptions" -> 0xFF806FC1
    "Salary" -> 0xFF16865B
    "Scholarship" -> 0xFF2F9E8F
    "Refund" -> 0xFF5B8C5A
    "Transfer In" -> 0xFF3B82A0
    "Investment Income" -> 0xFF8A6D3B
    "Part-time Job" -> 0xFF6E7D3A
    else -> DEFAULT_CATEGORY_COLOR
}

fun defaultCategoryIcon(name: String): String = when (name) {
    "Food" -> "●"; "Health" -> "+"; "Education" -> "A"; "Transport" -> "→"
    "Shopping" -> "◇"; "Entertainment" -> "☆"; "Housing" -> "⌂"; "Travel" -> "✦"
    "Communication" -> "⌁"; "Subscriptions" -> "↻"
    "Salary", "Scholarship", "Refund", "Transfer In", "Investment Income", "Part-time Job" -> "+"
    else -> "·"
}

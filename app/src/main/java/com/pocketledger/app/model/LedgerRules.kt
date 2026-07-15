package com.pocketledger.app.model

import java.time.Instant

object LedgerRules {
    fun recalculateBalances(
        accounts: List<LedgerAccount>,
        entries: List<LedgerEntry>,
        updatedAt: Long = Instant.now().toEpochMilli(),
    ): List<LedgerAccount> = accounts.map { account ->
        val impactMinor = entries.asSequence()
            .filter { it.accountId == account.id && it.currency == account.currency }
            .sumOf { entry ->
                val value = Money.toMinor(entry.amount, entry.currency)
                if (entry.type == EntryType.INCOME) value else -value
            }
        account.copy(
            balance = Money.fromMinor(Money.toMinor(account.initialBalance, account.currency) + impactMinor, account.currency),
            updatedAt = updatedAt,
        )
    }

    fun canDeleteAccount(accountId: String, entries: List<LedgerEntry>): Boolean = entries.none { it.accountId == accountId }
    fun canDeleteCategory(categoryId: String, entries: List<LedgerEntry>): Boolean = entries.none { it.categoryId == categoryId }

    fun activeAccounts(accounts: List<LedgerAccount>): List<LedgerAccount> = accounts.filterNot { it.archived }
    fun activeCategories(categories: List<LedgerCategory>, type: EntryType): List<LedgerCategory> = categories.filter { !it.archived && it.type == type }

    fun upsertAccount(accounts: List<LedgerAccount>, account: LedgerAccount): List<LedgerAccount> =
        if (accounts.any { it.id == account.id }) accounts.map { if (it.id == account.id) account else it } else accounts + account

    fun upsertCategory(categories: List<LedgerCategory>, category: LedgerCategory): List<LedgerCategory> =
        if (categories.any { it.id == category.id }) categories.map { if (it.id == category.id) category else it } else categories + category

    fun hasDuplicateAccountName(candidate: LedgerAccount, accounts: List<LedgerAccount>): Boolean =
        accounts.any { it.id != candidate.id && it.name.trim().equals(candidate.name.trim(), ignoreCase = true) }

    fun hasDuplicateCategoryName(candidate: LedgerCategory, categories: List<LedgerCategory>): Boolean =
        categories.any { it.id != candidate.id && it.name.trim().equals(candidate.name.trim(), ignoreCase = true) }

    fun canChangeAccountCurrency(accountId: String, entries: List<LedgerEntry>): Boolean =
        entries.none { it.accountId == accountId }

    fun canChangeCategoryType(categoryId: String, entries: List<LedgerEntry>): Boolean =
        entries.none { it.categoryId == categoryId }

    fun withMonthlyBudget(budget: Budget, currency: String, total: Double): Budget =
        if (budget.currency == currency) budget.copy(total = total)
        else Budget(total = total, categoryAmounts = emptyMap(), currency = currency)

    fun withCategoryBudget(budget: Budget, currency: String, categoryName: String, amount: Double): Budget {
        val base = if (budget.currency == currency) budget else Budget(total = 0.0, categoryAmounts = emptyMap(), currency = currency)
        return base.copy(categoryAmounts = base.categoryAmounts + (categoryName to amount))
    }
}

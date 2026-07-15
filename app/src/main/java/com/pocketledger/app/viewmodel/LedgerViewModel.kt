package com.pocketledger.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pocketledger.app.data.LedgerRepository
import com.pocketledger.app.model.*
import com.pocketledger.app.parser.NaturalLanguageParser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.*
import kotlinx.coroutines.launch

data class LedgerUiState(
    val data: StoredState = StoredState(),
    val search: String = "",
    val categoryFilter: String? = null,
    val accountFilter: String? = null,
    val dateFilter: LocalDate? = null,
    val userMessage: String? = null,
)

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val MAX_BACKUP_BYTES = 10 * 1024 * 1024
    }

    private data class PersistRequest(val generation: Long, val data: StoredState)

    private val repository = LedgerRepository(application)
    private val storageMutex = Mutex()
    private val saveRequests = Channel<PersistRequest>(Channel.CONFLATED)
    private var storageGeneration = 0L
    private val _uiState = MutableStateFlow(LedgerUiState(repository.load()))
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            for (request in saveRequests) {
                runCatching {
                    storageMutex.withLock {
                        if (request.generation == storageGeneration) repository.save(request.data)
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(userMessage = "Unable to save changes. Please try again.")
                }
            }
        }
    }

    fun parse(text: String): NaturalLanguageParser.ParseResult = NaturalLanguageParser.parse(
        text.take(LedgerConstraints.MAX_SENTENCE_LENGTH), _uiState.value.data.settings.currency, activeAccounts(), activeCategories()
    )

    fun saveEntry(candidate: LedgerEntry): Boolean {
        val normalized = candidate.copy(purpose = candidate.purpose.trim())
        if (
            !LedgerConstraints.isValidTransactionAmount(normalized.amount) ||
            normalized.purpose.isBlank() || normalized.purpose.length > LedgerConstraints.MAX_PURPOSE_LENGTH ||
            normalized.note.length > LedgerConstraints.MAX_NOTE_LENGTH ||
            normalized.rawText.length > LedgerConstraints.MAX_SENTENCE_LENGTH ||
            normalized.currency !in currencies
        ) return reject("This transaction contains an invalid or oversized field.")
        val data = _uiState.value.data
        val selectedAccount = normalized.accountId?.let { id -> data.accounts.firstOrNull { it.id == id } }
        if (normalized.accountId != null && (selectedAccount == null || selectedAccount.currency != normalized.currency)) {
            return reject("The selected account does not match the transaction currency.")
        }
        val selectedCategory = normalized.categoryId?.let { id -> data.categories.firstOrNull { it.id == id } }
        if (normalized.categoryId != null && (selectedCategory == null || selectedCategory.type != normalized.type)) {
            return reject("The selected category does not match the transaction type.")
        }
        val old = data.entries.firstOrNull { it.id == normalized.id }
        val saved = normalized.copy(updatedAt = Instant.now().toEpochMilli())
        val entries = if (old == null) data.entries + saved else data.entries.map { if (it.id == saved.id) saved else it }
        persist(data.copy(entries = entries.sortedByDescending { it.occurredAt }, accounts = recalculateBalances(data.accounts, entries)))
        return true
    }

    fun deleteEntry(entry: LedgerEntry) {
        val data = _uiState.value.data
        val entries = data.entries.filterNot { it.id == entry.id }
        persist(data.copy(entries = entries, accounts = recalculateBalances(data.accounts, entries)))
    }

    private fun recalculateBalances(accounts: List<LedgerAccount>, entries: List<LedgerEntry>): List<LedgerAccount> {
        return LedgerRules.recalculateBalances(accounts, entries)
    }

    fun addAccount(account: LedgerAccount): Boolean {
        val data = _uiState.value.data
        val normalized = account.copy(name = account.name.trim())
        if (
            normalized.name.isBlank() || normalized.name.length > LedgerConstraints.MAX_NAME_LENGTH ||
            normalized.type !in accountTypes || normalized.currency !in currencies ||
            !LedgerConstraints.isValidBalance(normalized.initialBalance) || !LedgerConstraints.isValidBalance(normalized.balance)
        ) return reject("This account contains an invalid or oversized field.")
        if (LedgerRules.hasDuplicateAccountName(normalized, data.accounts)) return reject("Account names must be unique.")
        val existing = data.accounts.firstOrNull { it.id == normalized.id }
        if (existing != null && existing.currency != normalized.currency && !LedgerRules.canChangeAccountCurrency(existing.id, data.entries)) {
            return reject("An account with transactions cannot change currency.")
        }
        val now = Instant.now().toEpochMilli()
        val transactionImpactMinor = data.entries.filter { it.accountId == normalized.id && it.currency == normalized.currency }.sumOf {
            val value = Money.toMinor(it.amount, it.currency)
            if (it.type == EntryType.INCOME) value else -value
        }
        val saved = if (existing == null) {
            normalized.copy(initialBalance = normalized.balance, createdAt = now, updatedAt = now)
        } else {
            val balanceChanged = Money.toMinor(normalized.balance, normalized.currency) != Money.toMinor(existing.balance, existing.currency)
            val initial = if (balanceChanged) Money.fromMinor(Money.toMinor(normalized.balance, normalized.currency) - transactionImpactMinor, normalized.currency) else normalized.initialBalance
            normalized.copy(initialBalance = initial, updatedAt = now)
        }
        val entries = if (existing != null && existing.name != saved.name) data.entries.map {
            if (it.accountId == saved.id) it.copy(accountName = saved.name, updatedAt = now) else it
        } else data.entries
        val accounts = LedgerRules.upsertAccount(data.accounts, saved)
        persist(data.copy(entries = entries, accounts = recalculateBalances(accounts, entries)))
        return true
    }

    fun archiveAccount(account: LedgerAccount): Boolean = addAccount(account.copy(archived = true))

    fun deleteUnusedAccount(account: LedgerAccount): Boolean {
        val data = _uiState.value.data
        if (!LedgerRules.canDeleteAccount(account.id, data.entries)) return false
        persist(data.copy(accounts = data.accounts.filterNot { it.id == account.id }))
        return true
    }

    fun accountUsageCount(accountId: String): Int = _uiState.value.data.entries.count { it.accountId == accountId }

    fun deleteAccountKeepingHistory(account: LedgerAccount) {
        val data = _uiState.value.data
        val entries = data.entries.map { if (it.accountId == account.id) it.copy(accountId = null, accountName = "Deleted Account", updatedAt = Instant.now().toEpochMilli()) else it }
        persist(data.copy(entries = entries, accounts = data.accounts.filterNot { it.id == account.id }))
    }

    fun moveAccountTransactions(source: LedgerAccount, target: LedgerAccount) {
        if (source.currency != target.currency) return
        val data = _uiState.value.data
        val entries = data.entries.map { if (it.accountId == source.id) it.copy(accountId = target.id, accountName = target.name, updatedAt = Instant.now().toEpochMilli()) else it }
        val accounts = data.accounts.filterNot { it.id == source.id }
        persist(data.copy(entries = entries, accounts = recalculateBalances(accounts, entries)))
    }

    fun addCategory(category: LedgerCategory): Boolean {
        val data = _uiState.value.data
        val normalized = category.copy(name = category.name.trim(), icon = category.icon.take(3))
        if (normalized.name.isBlank() || normalized.name.length > LedgerConstraints.MAX_NAME_LENGTH) {
            return reject("This category contains an invalid or oversized name.")
        }
        if (LedgerRules.hasDuplicateCategoryName(normalized, data.categories)) return reject("Category names must be unique.")
        val existing = data.categories.firstOrNull { it.id == normalized.id }
        if (existing != null && existing.type != normalized.type && !LedgerRules.canChangeCategoryType(existing.id, data.entries)) {
            return reject("A category with transactions cannot change type.")
        }
        val now = Instant.now().toEpochMilli()
        val saved = normalized.copy(updatedAt = now)
        val entries = if (existing != null && existing.name != saved.name) data.entries.map {
            if (it.categoryId == saved.id) it.copy(category = saved.name, updatedAt = now) else it
        } else data.entries
        val categories = LedgerRules.upsertCategory(data.categories, saved)
        val budget = if (existing != null && existing.name != saved.name && existing.name in data.budget.categoryAmounts) {
            data.budget.copy(categoryAmounts = data.budget.categoryAmounts.toMutableMap().apply {
                val amount = remove(existing.name)
                if (amount != null) put(saved.name, amount)
            })
        } else data.budget
        persist(data.copy(entries = entries, categories = categories, budget = budget))
        return true
    }

    fun archiveCategory(category: LedgerCategory) = addCategory(category.copy(archived = !category.archived))

    fun deleteUnusedCategory(category: LedgerCategory): Boolean {
        val data = _uiState.value.data
        if (!LedgerRules.canDeleteCategory(category.id, data.entries)) return false
        persist(data.copy(categories = data.categories.filterNot { it.id == category.id }, budget = data.budget.copy(categoryAmounts = data.budget.categoryAmounts - category.name)))
        return true
    }

    fun categoryUsageCount(categoryId: String): Int = _uiState.value.data.entries.count { it.categoryId == categoryId }

    fun deleteCategoryKeepingHistory(category: LedgerCategory) {
        val data = _uiState.value.data
        val entries = data.entries.map { if (it.categoryId == category.id) it.copy(categoryId = null, category = category.name, updatedAt = Instant.now().toEpochMilli()) else it }
        persist(data.copy(entries = entries, categories = data.categories.filterNot { it.id == category.id }, budget = data.budget.copy(categoryAmounts = data.budget.categoryAmounts - category.name)))
    }

    fun moveCategoryTransactions(source: LedgerCategory, target: LedgerCategory) {
        if (source.type != target.type) return
        val data = _uiState.value.data
        val entries = data.entries.map { if (it.categoryId == source.id) it.copy(categoryId = target.id, category = target.name, updatedAt = Instant.now().toEpochMilli()) else it }
        val sourceBudget = data.budget.categoryAmounts[source.name]
        val budget = data.budget.copy(categoryAmounts = data.budget.categoryAmounts.toMutableMap().apply {
            remove(source.name)
            if (sourceBudget != null) put(target.name, (this[target.name] ?: 0.0) + sourceBudget)
        })
        persist(data.copy(entries = entries, categories = data.categories.filterNot { it.id == source.id }, budget = budget))
    }

    fun setBudget(total: Double) {
        if (!LedgerConstraints.isValidBudget(total)) {
            reject("The budget amount is invalid.")
            return
        }
        val data = _uiState.value.data
        persist(data.copy(budget = LedgerRules.withMonthlyBudget(data.budget, data.settings.currency, total)))
    }

    fun setCategoryBudget(categoryName: String, amount: Double) {
        if (categoryName.isBlank() || !LedgerConstraints.isValidBudget(amount)) {
            reject("The category budget is invalid.")
            return
        }
        val data = _uiState.value.data
        persist(data.copy(budget = LedgerRules.withCategoryBudget(data.budget, data.settings.currency, categoryName, amount)))
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val data = _uiState.value.data
        persist(data.copy(settings = transform(data.settings)))
    }

    fun exportBackup(uri: Uri, passphrase: String) {
        val snapshot = _uiState.value.data
        viewModelScope.launch {
            runCatching {
                val contents = repository.exportBackup(snapshot, passphrase)
                withContext(Dispatchers.IO) {
                    require(contents.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) { "The backup is larger than 10 MB." }
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(contents) }
                        ?: error("The selected file could not be opened.")
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(userMessage = "Backup exported successfully.")
            }.onFailure {
                _uiState.value = _uiState.value.copy(userMessage = "Unable to export backup: ${it.message ?: "unknown error"}")
            }
        }
    }

    fun importBackup(uri: Uri, passphrase: String) {
        viewModelScope.launch {
            runCatching {
                val contents = withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                        require(descriptor.length < 0 || descriptor.length <= MAX_BACKUP_BYTES) { "The selected backup is larger than 10 MB." }
                    }
                    resolver.openInputStream(uri)?.use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8 * 1024)
                        var total = 0
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            total += read
                            require(total <= MAX_BACKUP_BYTES) { "The selected backup is larger than 10 MB." }
                            output.write(buffer, 0, read)
                        }
                        output.toString(Charsets.UTF_8.name())
                    } ?: error("The selected file could not be opened.")
                }
                storageMutex.withLock {
                    repository.importBackup(contents, passphrase).also { storageGeneration += 1 }
                }
            }.onSuccess { restored ->
                _uiState.value = LedgerUiState(
                    data = restored,
                    userMessage = "Backup restored successfully: ${restored.entries.size} transactions.",
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(userMessage = "Unable to restore backup: ${it.message ?: "invalid file"}")
            }
        }
    }

    fun setSearch(value: String) { _uiState.value = _uiState.value.copy(search = value) }
    fun setCategoryFilter(value: String?) { _uiState.value = _uiState.value.copy(categoryFilter = value) }
    fun setAccountFilter(value: String?) { _uiState.value = _uiState.value.copy(accountFilter = value) }
    fun setDateFilter(value: LocalDate?) { _uiState.value = _uiState.value.copy(dateFilter = value) }
    fun clearFilters() { _uiState.value = _uiState.value.copy(search = "", categoryFilter = null, accountFilter = null, dateFilter = null) }

    fun filteredEntries(): List<LedgerEntry> {
        val state = _uiState.value
        val query = state.search.trim().lowercase()
        return state.data.entries.filter { entry ->
            (query.isBlank() || listOf(entry.purpose, entry.note, entry.category, entry.accountName).any { it.lowercase().contains(query) }) &&
                (state.categoryFilter == null || entry.category == state.categoryFilter) &&
                (state.accountFilter == null || entry.accountId == state.accountFilter) &&
                (state.dateFilter == null || Instant.ofEpochMilli(entry.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate() == state.dateFilter)
        }
    }

    fun activeAccounts() = _uiState.value.data.accounts.filterNot { it.archived }
    fun activeCategories(type: EntryType? = null) = _uiState.value.data.categories.filter { !it.archived && (type == null || it.type == type) }

    fun entriesInCurrency(currency: String = _uiState.value.data.settings.currency) = _uiState.value.data.entries.filter { it.currency == currency }

    fun entriesInRange(range: String, now: LocalDate = LocalDate.now()): List<LedgerEntry> {
        val zone = ZoneId.systemDefault()
        val start = when (range) {
            "week" -> now.minusDays((now.dayOfWeek.value - 1).toLong())
            "year" -> now.withDayOfYear(1)
            else -> now.withDayOfMonth(1)
        }
        val end = when (range) {
            "week" -> start.plusWeeks(1)
            "year" -> start.plusYears(1)
            else -> start.plusMonths(1)
        }
        return entriesInCurrency().filter {
            val date = Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate()
            !date.isBefore(start) && date.isBefore(end)
        }
    }

    fun monthTotals(now: LocalDate = LocalDate.now()): Triple<Double, Double, Double> {
        return LedgerAnalytics.totals(entriesInRange("month", now), _uiState.value.data.settings.currency)
    }

    fun spendingSince(start: LocalDate, endExclusive: LocalDate): Double {
        val zone = ZoneId.systemDefault()
        return entriesInCurrency().filter {
            val date = Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate()
            it.type == EntryType.EXPENSE && !date.isBefore(start) && date.isBefore(endExclusive)
        }.sumOf { it.amount }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(userMessage = null) }

    private fun persist(data: StoredState) {
        _uiState.value = _uiState.value.copy(data = data)
        if (!saveRequests.trySend(PersistRequest(storageGeneration, data)).isSuccess) {
            _uiState.value = _uiState.value.copy(userMessage = "Unable to queue changes for saving. Please try again.")
        }
    }

    private fun reject(message: String): Boolean {
        _uiState.value = _uiState.value.copy(userMessage = message)
        return false
    }
}

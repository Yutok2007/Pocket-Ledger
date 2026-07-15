package com.pocketledger.app.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketledger.app.model.AppSettings
import com.pocketledger.app.model.Budget
import com.pocketledger.app.model.EntryType
import com.pocketledger.app.model.LedgerAccount
import com.pocketledger.app.model.LedgerCategory
import com.pocketledger.app.model.LedgerEntry
import com.pocketledger.app.model.StoredState
import com.pocketledger.app.model.TextSize
import com.pocketledger.app.model.ThemeMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LedgerRepositoryBackupTest {
    @Test
    fun completeBackupRoundTripPreservesLedgerData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = LedgerRepository(context)
        val account = LedgerAccount(
            id = "backup-account",
            name = "Travel card",
            type = "Bank Account",
            initialBalance = 100.0,
            balance = 130.0,
            currency = "USD",
            includeInAssets = false,
            archived = true,
            createdAt = 10,
            updatedAt = 20,
        )
        val category = LedgerCategory(
            id = "backup-category",
            name = "Freelance",
            type = EntryType.INCOME,
            icon = "+",
            color = 0xFF16865B,
            createdAt = 10,
            updatedAt = 20,
        )
        val entry = LedgerEntry(
            id = "backup-entry",
            type = EntryType.INCOME,
            amount = 30.0,
            currency = "USD",
            purpose = "Design work",
            categoryId = category.id,
            category = category.name,
            accountId = account.id,
            accountName = account.name,
            occurredAt = 30,
            note = "Restored note",
            rawText = "earned 30 dollars",
            createdAt = 30,
            updatedAt = 40,
        )
        val original = StoredState(
            entries = listOf(entry),
            accounts = listOf(account),
            categories = listOf(category),
            budget = Budget(total = 900.0, categoryAmounts = mapOf(category.name to 55.0), currency = "USD"),
            settings = AppSettings(theme = ThemeMode.DARK, textSize = TextSize.LARGE, currency = "USD", language = "zh-CN"),
        )

        val passphrase = "correct horse battery staple"
        val backup = repository.exportBackup(original, passphrase)
        val restored = repository.importBackup(backup, passphrase)

        assertTrue(BackupCrypto.isEncrypted(backup))
        assertTrue(!backup.contains("\"format\": \"pocket-ledger-backup\""))
        assertEquals(original.entries, restored.entries)
        assertEquals(original.accounts, restored.accounts)
        assertEquals(original.categories, restored.categories)
        assertEquals(original.budget, restored.budget)
        assertEquals(original.settings, restored.settings)
    }
}

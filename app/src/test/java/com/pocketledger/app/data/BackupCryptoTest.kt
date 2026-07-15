package com.pocketledger.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {
    private val passphrase = "correct horse battery staple"
    private val plaintext = """{"format":"pocket-ledger-backup","purpose":"private expense"}"""

    @Test
    fun encryptedBackupRoundTripPreservesPlaintext() {
        val encrypted = BackupCrypto.encrypt(plaintext, passphrase)

        assertTrue(BackupCrypto.isEncrypted(encrypted))
        assertTrue(!encrypted.contains("private expense"))
        assertEquals(plaintext, BackupCrypto.decrypt(encrypted, passphrase))
    }

    @Test
    fun encryptionUsesFreshSaltAndNonce() {
        val first = BackupCrypto.encrypt(plaintext, passphrase)
        val second = BackupCrypto.encrypt(plaintext, passphrase)

        assertNotEquals(first, second)
    }

    @Test
    fun wrongPassphraseAndTamperingAreRejected() {
        val encrypted = BackupCrypto.encrypt(plaintext, passphrase)
        val tampered = encrypted.dropLast(1) + if (encrypted.last() == 'A') "B" else "A"

        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(encrypted, "this password is wrong")
        }
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(tampered, passphrase)
        }
    }

    @Test
    fun shortPassphraseIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.encrypt(plaintext, "too short")
        }
    }
}

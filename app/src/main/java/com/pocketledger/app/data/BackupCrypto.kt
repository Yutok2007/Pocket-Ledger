package com.pocketledger.app.data

import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private const val MAGIC = "POCKET_LEDGER_ENCRYPTED_BACKUP"
    private const val VERSION = 1
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val TAG_BITS = 128
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    const val MIN_PASSPHRASE_LENGTH = 12

    fun isEncrypted(contents: String): Boolean = contents.startsWith("$MAGIC\n")

    fun encrypt(plaintext: String, passphrase: String): String {
        require(passphrase.length >= MIN_PASSPHRASE_LENGTH) {
            "Backup passphrase must contain at least $MIN_PASSPHRASE_LENGTH characters."
        }
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val key = deriveKey(passphrase, salt, ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD("$MAGIC:$VERSION".toByteArray(Charsets.UTF_8))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val encoder = Base64.getEncoder()
        return listOf(
            MAGIC,
            VERSION.toString(),
            ITERATIONS.toString(),
            encoder.encodeToString(salt),
            encoder.encodeToString(iv),
            encoder.encodeToString(ciphertext),
        ).joinToString("\n")
    }

    fun decrypt(contents: String, passphrase: String): String {
        val parts = contents.split('\n', limit = 6)
        require(parts.size == 6 && parts[0] == MAGIC) { "This is not an encrypted Pocket Ledger backup." }
        val version = parts[1].toIntOrNull()
        require(version == VERSION) { "This encrypted backup version is not supported." }
        val iterations = parts[2].toIntOrNull()
        require(iterations != null && iterations in 100_000..1_000_000) { "The backup key settings are invalid." }
        return try {
            val decoder = Base64.getDecoder()
            val salt = decoder.decode(parts[3])
            val iv = decoder.decode(parts[4])
            val ciphertext = decoder.decode(parts[5].trim())
            require(salt.size == SALT_BYTES && iv.size == IV_BYTES) { "The encrypted backup header is invalid." }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(passphrase, salt, iterations), GCMParameterSpec(TAG_BITS, iv))
            cipher.updateAAD("$MAGIC:$VERSION".toByteArray(Charsets.UTF_8))
            cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
        } catch (_: GeneralSecurityException) {
            throw IllegalArgumentException("The backup passphrase is incorrect or the file has been modified.")
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("The encrypted backup file is damaged or invalid.")
        }
    }

    private fun deriveKey(passphrase: String, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}

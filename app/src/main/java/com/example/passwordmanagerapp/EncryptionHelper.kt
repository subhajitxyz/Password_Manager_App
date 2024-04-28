package com.example.passwordmanagerapp

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class EncryptionHelper {

    private val key = "mysecretkey12345" // You can use any key (make it longer and more complex in production)
    private val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")

    @Throws(Exception::class)
    fun encrypt(string: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        // Generate a random IV (Initialization Vector) for each encryption
        val iv = generateRandomBytes(16) // 16 bytes for AES in CBC mode

        // Prepend the IV to the encrypted data for decryption
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        val encryptedBytes = cipher.doFinal(string.toByteArray(Charsets.UTF_8))

        // Combine the IV with the encrypted data for storage/transmission
        val combinedBytes = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combinedBytes, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combinedBytes, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(string: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        // Extract the IV from the combined data
        val combinedBytes = Base64.decode(string, Base64.DEFAULT)
        val iv = ByteArray(16)
        System.arraycopy(combinedBytes, 0, iv, 0, iv.size)
        val encryptedBytes = ByteArray(combinedBytes.size - iv.size)
        System.arraycopy(combinedBytes, iv.size, encryptedBytes, 0, encryptedBytes.size)

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        val randomBytes = ByteArray(size)
        // Use a secure random number generator (replace with a proper implementation)
        SecureRandom().nextBytes(randomBytes)
        return randomBytes
    }
}

package dev.zihad.remotesharding.util

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class AESGCMUtils(key: ByteArray) {
  private val key = SecretKeySpec(key, "AES")

  constructor(hexKey: String) : this(hexKey.hexToByteArray())

  fun encrypt(input: ByteArray): EncryptionResult {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv.copyOf()
    val result = cipher.doFinal(input)
    val encryptedData = result.copyOfRange(0, result.size - GCM_TAG_LENGTH)
    val tag = result.copyOfRange(result.size - GCM_TAG_LENGTH, result.size)
    return EncryptionResult(encryptedData, iv, tag)
  }

  fun decrypt(encryptionResult: EncryptionResult) =
    decrypt(encryptionResult.payload, encryptionResult.iv, encryptionResult.tag)

  private fun decrypt(input: ByteArray, iv: ByteArray, tag: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH * 8, iv))
    return cipher.doFinal(input + tag)
  }

  internal class EncryptionResult(val payload: ByteArray, val iv: ByteArray, val tag: ByteArray) {
    fun toByteArray(): ByteArray {
      val array = ByteArray(payload.size + iv.size + tag.size + 1)
      array[0] = iv.size.toByte()
      iv.copyInto(array, 1)
      tag.copyInto(array, iv.size + 1)
      payload.copyInto(array, tag.size + iv.size + 1)
      return array
    }

    companion object {
      fun fromByteArray(byteArray: ByteArray): EncryptionResult {
        val ivLength = byteArray[0].toInt()
        val iv = byteArray.sliceArray(IntRange(1, ivLength))
        val tag = byteArray.sliceArray(IntRange(ivLength + 1, ivLength + GCM_TAG_LENGTH))
        val payload = byteArray.sliceArray(IntRange(ivLength + GCM_TAG_LENGTH + 1, byteArray.size - 1))
        return EncryptionResult(payload, iv, tag)
      }
    }
  }

  companion object {
    private const val GCM_TAG_LENGTH = 16

    fun String.hexToByteArray(): ByteArray {
      require(length % 2 == 0) { "Hex string must have an even length" }
      val byteIterator = chunkedSequence(2)
        .map { it.toInt(16).toByte() }
        .iterator()

      return ByteArray(length / 2) { byteIterator.next() }
    }
  }
}

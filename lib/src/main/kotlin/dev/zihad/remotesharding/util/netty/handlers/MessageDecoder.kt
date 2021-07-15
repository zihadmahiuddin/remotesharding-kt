package dev.zihad.remotesharding.util.netty.handlers

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.client.HandshakeRequestMessage
import dev.zihad.remotesharding.messages.server.HandshakeResponseMessage
import dev.zihad.remotesharding.util.AESGCMUtils
import dev.zihad.remotesharding.util.Constants
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import kotlin.reflect.full.createInstance

internal class MessageDecoder(private val session: Session, aesSecretKey: String) : ByteToMessageDecoder() {
  private val aesGcmUtils = AESGCMUtils(aesSecretKey)

  override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
    // We need at least the same amount of bytes as the header to decode the packet
    if (`in`.readableBytes() < Constants.HEADER_LENGTH) return

    val readerIndex = `in`.readerIndex()

    val messageId = `in`.readShort()
    val length = `in`.readInt()

    if (`in`.readableBytes() < length) {
      // We haven't received the full message yet
      `in`.readerIndex(readerIndex)
      return
    }

    val messageClass = Message.Factory[messageId] // Find the message class corresponding to this id
    if (messageClass != null) {
      val message = messageClass.createInstance() // Create an instance of the message class
      message.session = session

      if (message !is HandshakeRequestMessage && message !is HandshakeResponseMessage) {
        val encryptionResultBytes = ByteArray(length)
        `in`.getBytes(`in`.readerIndex(), encryptionResultBytes)
        val encryptionResult = AESGCMUtils.EncryptionResult.fromByteArray(encryptionResultBytes)
        val decryptedBytes = aesGcmUtils.decrypt(encryptionResult)
        val decryptedByteBuf = ctx.alloc().buffer()
        decryptedByteBuf.writeBytes(decryptedBytes)
        message.decode(decryptedByteBuf)
        decryptedByteBuf.release()
      } else {
        message.decode(`in`)
      }
      out.add(message)
    } else {
      out.add(
        Message.UnknownMessage()
          .apply { id = messageId }) // Must add something to the list, so we add an instance of UnknownMessage
    }
    val bytesLeftToRead = (length + Constants.HEADER_LENGTH) - `in`.readerIndex()
    `in`.skipBytes(bytesLeftToRead)
  }
}

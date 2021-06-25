package dev.zihad.remotesharding.util.netty.handlers

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.client.HandshakeRequestMessage
import dev.zihad.remotesharding.messages.server.HandshakeResponseMessage
import dev.zihad.remotesharding.util.AESGCMUtils
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

internal class MessageEncoder(aesSecretKey: String) : MessageToByteEncoder<Message>() {
  private val aesGcmUtils = AESGCMUtils(aesSecretKey)

  override fun encode(ctx: ChannelHandlerContext, msg: Message, out: ByteBuf) {
    val byteBuf = ctx.alloc().buffer()
    msg.encode(byteBuf)
    val plaintextPayload = ByteArray(byteBuf.writerIndex())
    byteBuf.getBytes(0, plaintextPayload)
    byteBuf.release()

    val encryptedBytes =
      if (msg is HandshakeRequestMessage || msg is HandshakeResponseMessage) plaintextPayload
      else aesGcmUtils.encrypt(
        plaintextPayload
      ).toByteArray()

    out.writeShort(msg.id.toInt())

    out.writeInt(encryptedBytes.size)
    out.writeBytes(encryptedBytes)
  }
}

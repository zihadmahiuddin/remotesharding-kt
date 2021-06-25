package dev.zihad.remotesharding.messages.server

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.util.netty.readIString
import dev.zihad.remotesharding.util.netty.writeIString
import io.netty.buffer.ByteBuf

internal class LoginFailedMessage : Message() {
  override val id: Short = 203

  var reason: String = "Unknown"

  override fun decode(byteBuf: ByteBuf) {
    reason = byteBuf.readIString()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeIString(reason)
  }

  override fun processReceive() {
    session.logger.info("Login failed: $reason")
    session.channel?.close()
  }
}

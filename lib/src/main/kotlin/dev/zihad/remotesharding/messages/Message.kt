package dev.zihad.remotesharding.messages

import dev.zihad.remotesharding.messages.client.HandshakeRequestMessage
import dev.zihad.remotesharding.messages.client.HeartbeatMessage
import dev.zihad.remotesharding.messages.client.IdentifySentMessage
import dev.zihad.remotesharding.messages.client.LoginMessage
import dev.zihad.remotesharding.messages.server.*
import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.buffer.ByteBuf
import kotlin.reflect.KClass

internal abstract class Message {
  abstract val id: Short

  lateinit var session: Session

  open fun decode(byteBuf: ByteBuf) {}
  open fun encode(byteBuf: ByteBuf) {}
  open fun processSend() {}
  open fun processReceive() {}

  companion object Factory : HashMap<Short, KClass<out Message>>() {
    init {
      this[101] = HandshakeRequestMessage::class
      this[102] = LoginMessage::class
      this[103] = HeartbeatMessage::class
      this[104] = IdentifySentMessage::class

      this[201] = HandshakeResponseMessage::class
      this[202] = LoginOkMessage::class
      this[203] = LoginFailedMessage::class
      this[204] = HeartbeatAcknowledgedMessage::class
      this[205] = ShardInfoMessage::class
    }
  }

  class UnknownMessage : Message() {
    override var id: Short = 0
  }
}

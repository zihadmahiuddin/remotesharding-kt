package dev.zihad.remotesharding.messages.server

import dev.zihad.remotesharding.client.ClientSideSession
import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.client.LoginMessage
import io.netty.buffer.ByteBuf

internal class HandshakeResponseMessage : Message() {
  override val id: Short = 201

  var totalShardCount = 0

  override fun decode(byteBuf: ByteBuf) {
    totalShardCount = byteBuf.readInt()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeInt(totalShardCount)
  }

  override fun processReceive() {
    session.logger.debug("Handshake successful! Shard count: $totalShardCount")
    session.shardCount = totalShardCount

    (session as ClientSideSession).let {
      it.client.buildShardManager()
      it.sendMessage(
        LoginMessage().apply {
          botToken = it.botToken
        }
      )
    }
  }
}

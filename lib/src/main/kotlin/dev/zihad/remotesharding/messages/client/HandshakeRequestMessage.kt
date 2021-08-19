package dev.zihad.remotesharding.messages.client

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.server.HandshakeResponseMessage
import dev.zihad.remotesharding.messages.server.LoginFailedMessage
import dev.zihad.remotesharding.server.ServerSideSession
import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.buffer.ByteBuf

internal class HandshakeRequestMessage : Message() {
  override val id: Short = 101

  var shardCapacity: Short = 1

  override fun decode(byteBuf: ByteBuf) {
    shardCapacity = byteBuf.readShort()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeShort(shardCapacity.toInt())
  }

  override fun processReceive() {
    session.state = Session.State.Handshake
    session.logger.debug("Client ${session.channel?.remoteAddress()} sent handshake request with shard capacity $shardCapacity.")

    (session as? ServerSideSession)!!.let {
      synchronized(it.server) {
        val freeShardIds = it.server.connectedShards.mapIndexedNotNull { i, item -> if (item == null) i else null }
        val nextShardId =
          (freeShardIds - it.server.queuedShardIds).firstOrNull() ?: -1
        session.sendMessage(
          if (nextShardId < 0) LoginFailedMessage().apply { reason = "No more shards needed" }
          else {
            it.shardCapacity = shardCapacity.toInt()
            HandshakeResponseMessage().apply {
              totalShardCount = it.server.totalShardCount
            }
          }
        )
      }
    }
  }
}

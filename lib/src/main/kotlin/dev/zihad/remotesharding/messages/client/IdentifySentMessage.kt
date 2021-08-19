package dev.zihad.remotesharding.messages.client

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.server.ServerSideSession
import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.buffer.ByteBuf

internal class IdentifySentMessage : Message() {
  override val id: Short = 104

  var shardId: Short = -1

  override fun decode(byteBuf: ByteBuf) {
    shardId = byteBuf.readShort()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeShort(shardId.toInt())
  }

  override fun processReceive() {
    session.state = Session.State.IdentifySent
    session.logger.info("Client ${session.channel?.remoteAddress()}[${session.shardIds}] has sent IDENTIFY")

    (session as? ServerSideSession)?.let {
      val shardIdInt = shardId.toInt()
      synchronized(it.server) {
        it.server.buckets[shardIdInt % it.server.buckets.size].scheduleProcessingNextShard()
        it.server.connectedShards[shardIdInt] = it
        it.server.queuedShardIds.remove(shardIdInt)
      }
    }
  }
}

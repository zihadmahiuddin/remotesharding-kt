package dev.zihad.remotesharding.messages.server

import dev.zihad.remotesharding.client.ClientSideSession
import dev.zihad.remotesharding.messages.Message
import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory

internal class ShardInfoMessage : Message() {
  override val id: Short = 205

  var shardId: Int = -1

  override fun decode(byteBuf: ByteBuf) {
    shardId = byteBuf.readInt()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeInt(shardId)
  }

  override fun processSend() {
    session.shardIds.add(shardId)
    session.logger = LoggerFactory.getLogger("Session-${session.shardIds.joinToString(",")}")
  }

  override fun processReceive() {
    session.shardIds.add(shardId)
    session.logger = LoggerFactory.getLogger("Session-${session.shardIds}")
    (session as ClientSideSession).client.startShard(shardId)
  }
}

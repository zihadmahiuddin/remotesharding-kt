package dev.zihad.remotesharding.messages.client

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.server.LoginFailedMessage
import dev.zihad.remotesharding.messages.server.LoginOkMessage
import dev.zihad.remotesharding.server.ServerSideSession
import dev.zihad.remotesharding.util.netty.handlers.Session
import dev.zihad.remotesharding.util.netty.readIString
import dev.zihad.remotesharding.util.netty.writeIString
import io.netty.buffer.ByteBuf
import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit

internal class LoginMessage : Message() {
  override val id: Short = 102

  var botToken: String = ""

  override fun decode(byteBuf: ByteBuf) {
    botToken = byteBuf.readIString()
  }

  override fun encode(byteBuf: ByteBuf) {
    byteBuf.writeIString(botToken)
  }

  override fun processReceive() {
    if (session.botToken != botToken) {
      session.sendMessage(
        LoginFailedMessage().apply { reason = "Incorrect token provided." }
      )
    } else {
      (session as? ServerSideSession)!!.let {
        synchronized(it.server) {
          val freeShardIds = it.server.connectedShards.mapIndexedNotNull { i, it -> if (it == null) i else null }
          val nextShardIds =
            (freeShardIds - it.server.queuedShardIds).take(it.shardCapacity)
          val nextShardIdsGroupedByBucket = nextShardIds.groupBy { shardId -> shardId % it.server.buckets.size }
          nextShardIdsGroupedByBucket.forEach { (k, v) ->
            it.server.queuedShardIds.addAll(v)
            it.server.buckets[k].queueShard(it, v)
          }
        }
        session.let { session ->
          session.state = Session.State.Login
          session.channel?.pipeline()
            ?.addBefore("Session", "ReadTimeoutHandler", ReadTimeoutHandler(1, TimeUnit.MINUTES))
        }
        it.sendMessage(LoginOkMessage())
      }
    }
  }
}

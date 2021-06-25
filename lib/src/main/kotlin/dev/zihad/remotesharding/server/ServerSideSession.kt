package dev.zihad.remotesharding.server

import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

internal class ServerSideSession(
  channel: Channel?,
  val server: Server,
  botToken: String,
  override var shardCount: Int
) : Session(channel, botToken) {

  override fun channelInactive(ctx: ChannelHandlerContext) {
    if (state >= State.Login) {
      synchronized(server) {
        shardIds.forEach {
          server.connectedShards[it] = null
        }
        server.connectedClients.remove(channel)
      }
      logger.info("Client ${channel?.remoteAddress()}[${shardIds}] disconnected.")
    }
    super.channelInactive(ctx)
  }
}

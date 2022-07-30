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
  override fun channelActive(ctx: ChannelHandlerContext) {
    super.channelActive(ctx)
    logger.info("Client ${channel?.remoteAddress()} connected.")
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    synchronized(server) {
      shardIds.forEach {
        server.buckets[it % server.buckets.size].dequeueShard(it)
        server.connectedShards[it] = null
      }
      server.connectedClients.remove(channel)
    }
    logger.info("Client ${channel?.remoteAddress()}[${shardIds}] disconnected.")
    super.channelInactive(ctx)
  }
}

package dev.zihad.remotesharding.client

import dev.zihad.remotesharding.messages.client.HandshakeRequestMessage
import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext

internal class ClientSideSession(
  channel: Channel?,
  val client: Client,
  botToken: String
) : Session(channel, botToken) {
  override fun channelActive(ctx: ChannelHandlerContext) {
    super.channelActive(ctx)

    logger.debug("Initializing Session with ${ctx.channel().remoteAddress()}")
    sendMessage(HandshakeRequestMessage().apply { shardCapacity = client.shardCapacity.toShort() })
  }
}

package dev.zihad.remotesharding.util.netty.handlers

import dev.zihad.remotesharding.server.ServerSideSession
import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.LoggerFactory

internal class ExceptionHandler(private val session: Session) : ChannelHandlerAdapter() {
  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    if (cause is ReadTimeoutException && session is ServerSideSession) {
      logger.warn("Client ${ctx.channel().remoteAddress()}[${session.shardIds}] timed out.")
    } else {
      logger.error("Exception on channel ${ctx.channel().remoteAddress()}:", cause)
    }
    ctx.close()
  }

  companion object {
    val logger = LoggerFactory.getLogger(ExceptionHandler::class.java)!!
  }
}

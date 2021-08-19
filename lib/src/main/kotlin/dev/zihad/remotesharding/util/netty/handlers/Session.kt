package dev.zihad.remotesharding.util.netty.handlers

import dev.zihad.remotesharding.messages.Message
import io.netty.channel.*
import io.netty.util.ReferenceCountUtil
import org.slf4j.LoggerFactory

internal abstract class Session(
  var channel: Channel?,
  val botToken: String
) : ChannelDuplexHandler() {
  internal var shardCapacity = -1

  var logger = LoggerFactory.getLogger(Session::class.java)!!

  enum class State {
    None,
    Handshake,
    Login,
    IdentifySent
  }

  var state = State.None

  open var shardIds = mutableListOf<Int>()
  open var shardCount: Int = 1

  override fun channelActive(ctx: ChannelHandlerContext) {
    channel = ctx.channel()
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    channel = null
  }

  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    var release = true
    try {
      if (msg is Message) {
        messageReceived(msg)
      } else {
        release = false
        ctx.fireChannelRead(msg)
      }
    } finally {
      if (release) {
        ReferenceCountUtil.release(msg)
      }
    }
  }

  private fun messageReceived(msg: Message) {
    if (msg is Message.UnknownMessage) {
      logger.info("Unknown message received: ${msg.id}")
    } else {
      msg.processReceive()
    }
  }

  override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise) {
    super.write(ctx, msg, promise)
    if (msg is Message) {
      msg.session = this
    }
  }

  fun sendMessage(message: Message): ChannelFuture? {
    return channel?.writeAndFlush(message)
  }
}

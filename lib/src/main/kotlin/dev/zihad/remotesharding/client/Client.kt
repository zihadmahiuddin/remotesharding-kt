package dev.zihad.remotesharding.client

import dev.zihad.remotesharding.messages.client.IdentifySentMessage
import dev.zihad.remotesharding.util.netty.NettyUtils
import dev.zihad.remotesharding.util.netty.handlers.ExceptionHandler
import dev.zihad.remotesharding.util.netty.handlers.MessageDecoder
import dev.zihad.remotesharding.util.netty.handlers.MessageEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.SocketChannel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import io.netty.channel.ChannelInitializer as NettyChannelInitializer

internal typealias ShardManagerBuildHandler = (ShardManager) -> Unit

class Client(
  private val shardManagerBuilder: DefaultShardManagerBuilder,
  private val address: String,
  private val port: Int,
  private val botToken: String,
  private val encryptionKey: String,
  internal val shardCapacity: Int = 1
) {
  @Suppress("UNUSED")
  constructor(
    shardManagerBuilder: DefaultShardManagerBuilder,
    address: String,
    port: Int,
    botToken: String,
    encryptionKey: String
  ) : this(shardManagerBuilder, address, port, botToken, encryptionKey, 1)

  internal var session: ClientSideSession? = null

  private var reconnectAttempts = 0

  private lateinit var shardManager: ShardManager

  private val onBuildListeners = mutableSetOf<ShardManagerBuildHandler>()

  internal fun buildShardManager() {
    shardManager = shardManagerBuilder
      .setShardsTotal(session!!.shardCount)
      .setShards()
      .addEventListeners(JDAEventListener(this))
      .build(false)
    onBuildListeners.forEach {
      it(shardManager)
    }
  }

  fun start() {
    val eventGroup = NettyUtils.getEventLoopGroup()
    val bootstrap = Bootstrap().apply {
      group(eventGroup)
      channel(NettyUtils.getSocketChannelClass())
      handler(ChannelHandler(this@Client))
    }
    try {
      val channel = bootstrap
        .connect(InetSocketAddress(address, port))
        .sync()
        .channel()
      logger.info("Client connected to ${channel.remoteAddress()}")
      reconnectAttempts = 0
      channel.closeFuture().addListener {
        eventGroup.shutdownGracefully()
        start()
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      if (reconnectAttempts < 5) {
        logger.info("Failed to connect to the server. Waiting for 5 seconds before reconnecting...")
        Thread.sleep(5000)
        reconnectAttempts++
        eventGroup.shutdownGracefully()
        start()
      } else {
        logger.info("Failed to connect $reconnectAttempts times in a row, exiting...")
        session = null
        eventGroup.shutdownGracefully()
      }
    }
  }

  @Suppress("UNUSED")
  fun onShardManagerBuild(handler: ShardManagerBuildHandler) {
    onBuildListeners.add(handler)
  }

  internal fun startShard(shardId: Int) {
    shardManager.start(shardId)
  }

  internal class JDAEventListener(private val client: Client) : EventListener {
    override fun onEvent(event: GenericEvent) {
      if (event is StatusChangeEvent) {
        if (event.oldStatus == JDA.Status.IDENTIFYING_SESSION && event.newStatus == JDA.Status.AWAITING_LOGIN_CONFIRMATION) {
          client.session!!.sendMessage(IdentifySentMessage().apply { shardId = event.jda.shardInfo.shardId.toShort() })
        }
      }
    }
  }

  internal class ChannelHandler(private val client: Client) : NettyChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      client.session = ClientSideSession(ch, client, client.botToken)
      client.session!!.shardCapacity = client.shardCapacity
      ch.pipeline().addLast(
        MessageDecoder(client.session!!, client.encryptionKey),
        MessageEncoder(client.encryptionKey),
        client.session!!,
        ExceptionHandler(client.session!!)
      )
    }
  }

  internal companion object {
    private val logger = LoggerFactory.getLogger(Client::class.java)!!
  }
}

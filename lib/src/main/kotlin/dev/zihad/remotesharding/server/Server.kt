package dev.zihad.remotesharding.server

import dev.zihad.remotesharding.util.discord.DiscordGatewayUtils
import dev.zihad.remotesharding.util.netty.NettyUtils
import dev.zihad.remotesharding.util.netty.awaitSuspending
import dev.zihad.remotesharding.util.netty.handlers.ExceptionHandler
import dev.zihad.remotesharding.util.netty.handlers.MessageDecoder
import dev.zihad.remotesharding.util.netty.handlers.MessageEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.socket.SocketChannel
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors
import kotlin.properties.Delegates
import io.netty.channel.ChannelInitializer as NettyChannelInitializer

class Server(
  private val address: String,
  private val port: Int,
  private val botToken: String,
  private val encryptionKey: String,
  private val providedShardCount: Int? = null
) {
  internal var totalShardCount by Delegates.notNull<Int>()

  internal val buckets by lazy { Array(gatewayInfo.sessionStartLimit.maxConcurrency) { Bucket(it) } }
  internal val connectedClients = mutableMapOf<Channel, ServerSideSession>()
  internal val connectedShards by lazy { arrayOfNulls<ServerSideSession>(totalShardCount) }
  internal val queuedShardIds: Set<Int>
    get() {
      return buckets.flatMap { it.queuedShards.keys }.toSet()
    }

  private val serverCoroutineScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

  private lateinit var gatewayInfo: DiscordGatewayUtils.GatewayInfo

  fun start() {
    runBlocking {
      serverCoroutineScope.launch {
        for ((channel, session) in connectedClients) {
          if (
            session.lastHeartbeatAt != null &&
            session.lastHeartbeatAt!! + 15000 < System.currentTimeMillis()
          ) {
            channel.close()
          }
        }
        delay(5000)
      }

      serverCoroutineScope.launch {
        gatewayInfo = DiscordGatewayUtils.getGatewayInfo(botToken)
        totalShardCount = if (providedShardCount == null || providedShardCount == 0)
          gatewayInfo.shards.also {
            logger.info("Shard count not provided, using recommended shard count $it")
          } else providedShardCount

        if (gatewayInfo.sessionStartLimit.remaining < 1) {
          throw Error("Session rate limit reached. Resets at ${Date(System.currentTimeMillis() + gatewayInfo.sessionStartLimit.resetAfter)}")
        }
        val eventGroup = NettyUtils.getEventLoopGroup()
        val workerGroup = NettyUtils.getEventLoopGroup()
        try {
          val serverBootstrap = ServerBootstrap().apply {
            group(eventGroup, workerGroup)
            channel(NettyUtils.getServerSocketChannelClass())
            childHandler(ChannelInitializer(this@Server))
          }
          serverBootstrap
            .bind(InetSocketAddress(address, port))
            .awaitSuspending()
            .channel()
            .also {
              logger.info("Server started on ${it.localAddress()}")
            }
            .closeFuture()
            .awaitSuspending()
        } finally {
          eventGroup.shutdownGracefully()
          workerGroup.shutdownGracefully()
        }
        serverCoroutineScope.cancel()
      }.join()
    }
  }

  internal class ChannelInitializer(private val server: Server) : NettyChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
      val session = ServerSideSession(ch, server, server.botToken, server.providedShardCount!!)
      server.connectedClients[ch] = session

      val pipeline = ch.pipeline()
      pipeline.addLast("MessageDecoder", MessageDecoder(session, server.encryptionKey))
      pipeline.addLast("MessageEncoder", MessageEncoder(server.encryptionKey))
      pipeline.addLast("Session", session)
      pipeline.addLast("ExceptionHandler", ExceptionHandler(session))
    }
  }

  internal companion object {
    private val logger = LoggerFactory.getLogger(Server::class.java)!!
  }
}

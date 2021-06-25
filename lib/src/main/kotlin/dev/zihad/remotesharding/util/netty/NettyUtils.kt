package dev.zihad.remotesharding.util.netty

import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.kqueue.KQueueSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

internal object NettyUtils {
  fun getEventLoopGroup(): EventLoopGroup {
    return when {
      KQueue.isAvailable() -> KQueueEventLoopGroup()
      Epoll.isAvailable() -> EpollEventLoopGroup()
      else -> NioEventLoopGroup()
    }
  }

  fun getSocketChannelClass(): Class<out SocketChannel> {
    return when {
      KQueue.isAvailable() -> KQueueSocketChannel::class.java
      Epoll.isAvailable() -> EpollSocketChannel::class.java
      else -> NioSocketChannel::class.java
    }
  }

  fun getServerSocketChannelClass(): Class<out ServerSocketChannel> {
    return when {
      KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
      Epoll.isAvailable() -> EpollServerSocketChannel::class.java
      else -> NioServerSocketChannel::class.java
    }
  }
}

package dev.zihad.remotesharding.util.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.util.CharsetUtil
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Waits until the ChannelFuture is complete or cancelled and returns the result accordingly
 */
internal suspend fun ChannelFuture.awaitSuspending(): ChannelFuture {
  return suspendCancellableCoroutine { cont ->
    addListener {
      if (it.isCancelled) {
        cont.cancel(it.cause())
      } else {
        cont.resume(this)
      }
    }
  }
}

/**
 * Reads an "integer length prefixed utf-8 encoded string"
 */
internal fun ByteBuf.readIString(): String {
  val length = readInt()
  if (length == 0) return ""
  return readCharSequence(length, CharsetUtil.UTF_8).toString()
}

/**
 * Writes an "integer length prefixed utf-8 encoded string"
 */
internal fun ByteBuf.writeIString(string: String) {
  writeInt(string.length)
  if (string.isNotEmpty()) writeCharSequence(string, CharsetUtil.UTF_8)
}

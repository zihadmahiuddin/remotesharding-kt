package dev.zihad.remotesharding.server

import dev.zihad.remotesharding.messages.server.ShardInfoMessage
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class Bucket(private val id: Int) : Runnable {
  private val scheduledExecutorService by lazy { Executors.newSingleThreadScheduledExecutor() }
  private val queuedShards = mutableMapOf<Int, ServerSideSession>()

  private var canProcessNextShard = true

  private val logger = LoggerFactory.getLogger("Bucket-${id}")

  init {
    scheduledExecutorService.scheduleAtFixedRate(this, 10, 5, TimeUnit.SECONDS)
  }

  fun queueShard(session: ServerSideSession, shardIds: List<Int>) {
    synchronized(queuedShards) {
      shardIds.forEach { queuedShards[it] = session }
    }
    logger.debug("Queued shards ${queuedShards.keys.joinToString(", ") { it.toString() }} on bucket $id")
  }

  fun scheduleProcessingNextShard() {
    scheduledExecutorService.schedule({
      canProcessNextShard = true
    }, 5, TimeUnit.SECONDS)
  }

  override fun run() {
    if (!canProcessNextShard) return

    synchronized(queuedShards) {
      val nextShardToProcess = queuedShards.keys.firstOrNull() ?: return
      logger.debug("Processing shard $nextShardToProcess")
      queuedShards[nextShardToProcess]?.apply {
        shardIds.add(nextShardToProcess)
        sendMessage(
          ShardInfoMessage().apply {
            shardId = nextShardToProcess
          }
        )
      }
      queuedShards.remove(nextShardToProcess)
      canProcessNextShard = false
    }
  }
}

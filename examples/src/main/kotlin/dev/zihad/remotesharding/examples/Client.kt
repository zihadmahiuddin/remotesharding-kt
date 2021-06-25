package dev.zihad.remotesharding.examples

import dev.zihad.remotesharding.client.Client
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder

object Client : ListenerAdapter() {
  @JvmStatic
  fun main(args: Array<String>) {
    val host = System.getenv("HOST") ?: "0.0.0.0"
    val port = System.getenv("PORT")?.toIntOrNull() ?: 5252
    val botToken = System.getenv("BOT_TOKEN") ?: throw Exception("Token must be provided")
    val encryptionKey = System.getenv("ENCRYPTION_KEY") ?: throw Exception("Encryption be provided")

    repeat(1) {
      val shardManagerBuilder = DefaultShardManagerBuilder.createLight(botToken)
        .setActivity(Activity.playing("Testing"))
        .addEventListeners(this)

      val client = Client(shardManagerBuilder, host, port, botToken, encryptionKey)
      client.start()
    }
  }

  override fun onReady(event: ReadyEvent) {
    println(
      "Shard ${event.jda.shardInfo.shardId} started with ${event.jda.guilds.size} guilds: ${
        event.jda.guilds.joinToString(
          ", "
        ) { "${it.name}(${it.id})" }
      }"
    )
  }

  override fun onMessageReceived(event: MessageReceivedEvent) {
    println("Shard ${event.jda.shardInfo.shardId} received ${event.message.contentRaw}")
  }
}

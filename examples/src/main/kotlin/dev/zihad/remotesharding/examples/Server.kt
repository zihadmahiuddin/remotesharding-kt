package dev.zihad.remotesharding.examples

import dev.zihad.remotesharding.server.Server


object Server {
  @JvmStatic
  fun main(args: Array<String>) {
    val host = System.getenv("HOST") ?: "0.0.0.0"
    val port = System.getenv("PORT")?.toIntOrNull() ?: 5252
    val botToken = System.getenv("BOT_TOKEN") ?: throw Exception("Token must be provided")
    val encryptionKey = System.getenv("ENCRYPTION_KEY") ?: throw Exception("Encryption key be provided")
    val shardCount = System.getenv("SHARD_COUNT")?.toIntOrNull()
    val server = Server(host, port, botToken, encryptionKey, shardCount)
    server.start()
  }
}

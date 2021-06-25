package dev.zihad.remotesharding.examples;

import dev.zihad.remotesharding.server.Server;

import java.util.Optional;

public class JavaServer {
  public static void main(String[] args) throws Exception {
    String address = Optional.ofNullable(System.getenv("HOST")).orElse("0.0.0.0");

    int port = 5252;
    try {
      port = Integer.parseInt(System.getenv("PORT"));
    } catch (NumberFormatException ignored) {
    }

    String botToken = Optional.ofNullable(System.getenv("BOT_TOKEN")).orElseThrow(() -> new Exception("Bot token must be provided"));
    String encryptionKey = Optional.ofNullable(System.getenv("ENCRYPTION_KEY")).orElseThrow(() -> new Exception("Encryption key must be provided"));

    int shardCount = 0;
    try {
      shardCount = Integer.parseInt(System.getenv("SHARD_COUNT"));
    } catch (NumberFormatException ignored) {
    }

    Server server = new Server(address, port, botToken, encryptionKey, shardCount);
    server.start();
  }
}

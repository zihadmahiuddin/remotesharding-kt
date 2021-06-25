package dev.zihad.remotesharding.examples;

import dev.zihad.remotesharding.client.Client;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import java.util.Optional;
import java.util.stream.Collectors;

public class JavaClient extends ListenerAdapter {
  private JavaClient() {
  }

  public static void main(String[] args) throws Exception {
    String address = Optional.ofNullable(System.getenv("HOST")).orElse("0.0.0.0");

    int port = 5252;
    try {
      port = Integer.parseInt(System.getenv("PORT"));
    } catch (NumberFormatException ignored) {
    }

    String botToken = Optional.ofNullable(System.getenv("BOT_TOKEN")).orElseThrow(() -> new Exception("Bot token must be provided"));
    String encryptionKey = Optional.ofNullable(System.getenv("ENCRYPTION_KEY")).orElseThrow(() -> new Exception("Encryption key must be provided"));

    DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder.createLight(botToken)
        .setActivity(Activity.playing("Testing"))
        .addEventListeners(new JavaClient());

    Client client = new Client(shardManagerBuilder, address, port, botToken, encryptionKey);
    client.start();
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    System.out.println("Shard " + event.getJDA().getShardInfo().getShardId() + " received " + event.getMessage().getContentDisplay());
  }

  @Override
  public void onReady(ReadyEvent event) {
    System.out.println(
        "Shard " + event.getJDA().getShardInfo().getShardId() + " started with " + event.getJDA().getGuilds().size() + " guilds: " +
            event.getJDA().getGuilds().stream().map(x -> x.getName() + "(" + x.getId() + ")").collect(Collectors.joining())
    );
  }
}

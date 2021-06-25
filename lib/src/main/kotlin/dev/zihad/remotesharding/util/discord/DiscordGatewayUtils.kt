package dev.zihad.remotesharding.util.discord

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal object DiscordGatewayUtils {
  private const val gatewayVersion = "9"

  private const val apiBaseUrl = "https://discord.com/api"
  private const val apiUrl = "${apiBaseUrl}/v${gatewayVersion}"

  private val httpClient = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
      })
    }
  }

  @Serializable
  data class GatewayInfo(
    val url: String,
    val shards: Int,
    @SerialName("session_start_limit") val sessionStartLimit: SessionStartLimit
  ) {
    @Serializable
    data class SessionStartLimit(
      val total: Int,
      val remaining: Int,
      @SerialName("reset_after") val resetAfter: Int,
      @SerialName("max_concurrency") val maxConcurrency: Int
    )
  }

  suspend fun getGatewayInfo(botToken: String): GatewayInfo {
    return httpClient.get("${apiUrl}/gateway/bot") {
      header("Authorization", "Bot $botToken")
    }
  }
}

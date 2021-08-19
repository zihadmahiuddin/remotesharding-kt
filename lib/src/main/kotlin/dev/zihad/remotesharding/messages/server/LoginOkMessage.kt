package dev.zihad.remotesharding.messages.server

import dev.zihad.remotesharding.messages.Message
import dev.zihad.remotesharding.messages.client.HeartbeatMessage
import dev.zihad.remotesharding.util.netty.handlers.Session
import io.netty.handler.timeout.ReadTimeoutHandler
import java.util.concurrent.TimeUnit

internal class LoginOkMessage : Message() {
  override val id: Short = 202

  override fun processReceive() {
    session.let { session ->
      session.state = Session.State.Login
      session.logger.info("Login successful!")
      session.channel?.eventLoop()?.scheduleAtFixedRate({
        session.sendMessage(
          HeartbeatMessage()
        )
      }, 0, 10, TimeUnit.SECONDS)
    }
  }
}

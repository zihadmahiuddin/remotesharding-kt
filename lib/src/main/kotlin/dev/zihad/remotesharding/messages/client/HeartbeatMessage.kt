package dev.zihad.remotesharding.messages.client

import dev.zihad.remotesharding.messages.Message

internal class HeartbeatMessage : Message() {
  override val id: Short = 103

  override fun processReceive() {
    session.logger.debug("Heartbeat received")
  }
}

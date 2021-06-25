package dev.zihad.remotesharding.messages.server

import dev.zihad.remotesharding.messages.Message

internal class HeartbeatAcknowledgedMessage : Message() {
  override val id: Short = 204

  override fun processReceive() {
    session.logger.debug("Heartbeat acknowledged")
  }
}

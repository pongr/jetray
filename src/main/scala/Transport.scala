package com.pongr.jetray

import akka.actor._
import javax.mail.{Transport, Session}
import javax.mail.internet.MimeMessage
import Timing._
import TestRun._

/*
TransportActor wraps one Transport object
Transport is basically one open SMTP connection
It's expensive to open a new connection
Keep it open and reuse it, so we can shove multiple messages through it

Supervisor needs to:
  - on com.sun.mail.smtp.SMTPSendFailedException: restart & resend the message
  - on other exceptions just restart and we'll lose the message
*/

class TransportActor(
  session: Session,
  host: => String,
  port: Int,
  user: String,
  password: String,
  timing: ActorRef
) extends Actor with ActorLogging {
  var transportOption: Option[Transport] = None

  override def preStart() {
    log.debug("Connecting to {}:{} as {}...", host, port, user)
    val transport = session.getTransport("smtp")
    transport.connect(host, port, user, password)
    log.debug("Connected to {}:{} as {}", host, port, user)
    transportOption = Some(transport)
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    if (reason.isInstanceOf[com.sun.mail.smtp.SMTPSendFailedException]) {
      message foreach { self forward _ } //try to resend this email later
      log.warning("Resending {}...", message)
    }
    super.preRestart(reason, message)
  }

  override def postStop() {
    transportOption foreach { _.close() }
    log.debug("Closed connection to {}:{}", host, port)
  }

  def receive = {
    case message: MimeMessage => 
      for (transport <- transportOption) { //TODO should we spawn a child actor to actually send the message?
        val (runId, emailId) = getIds(message) //TODO what if we can't extract runId & emailId out of message?
        val t1 = System.currentTimeMillis
        transport.sendMessage(message, message.getAllRecipients)
        val t2 = System.currentTimeMillis
        timing ! preSentEmail(runId, emailId, t1)
        timing ! postSentEmail(runId, emailId, t2)
      }
  }
}

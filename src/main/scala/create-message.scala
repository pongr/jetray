package com.pongr.jetray

import javax.mail._
import javax.mail.internet._

/** Uses "runId-emailId" for both subject and body. */
class SimpleCreateMessage(session: Session, from: String, to: String) extends ((String, Int) => MimeMessage) {
  def apply(runId: String, emailId: Int) = {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(from))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    message.setSubject(runId + "-" + emailId)
    message.setText(runId + "-" + emailId)
    message
  }
}

package com.pongr.jetray

import akka.actor._
import akka.dispatch._
import akka.pattern.pipe
import javax.mail.internet.MimeMessage

object TestRun {
  case object Send
  case class SendOne(runId: String, emailId: Int)

  val RunIdHeader = "X-Jetray-runId"
  val EmailIdHeader = "X-Jetray-emailId"
  def storeIds(message: MimeMessage, runId: String, emailId: Int) {
    //TODO only add if header doesn't already exist
    message.addHeader(RunIdHeader, runId)
    message.addHeader(EmailIdHeader, emailId.toString)
  }
  def getIds(message: MimeMessage): (String, Int) = {
    (message.getHeader(RunIdHeader).head, message.getHeader(EmailIdHeader).head.toInt)
  }
}

/*
TestRun represents the entire test run
Gets a Send message every n msec, up to m times
Kick off the sending of an email on Send
Spawn a child actor for each email
  - in case the sending crashes for some reason
  - in case the sending takes too long
*/

class TestRun(
  runId: String, 
  createMessage: (String, Int) => MimeMessage, 
  transport: ActorRef,
  timing: ActorRef
) extends Actor with ActorLogging {
  import TestRun._
  import Timing._
  var emailId = 0

  //TODO supervisor strategy: we could try to retry a couple times, but have to get that SendOne msg somehow... or maybe just stop it & don't care
  //but really, what could fail? the createMessage() call? probably not that important...

  def receive = {
    case Send => 
      emailId += 1
      context.actorOf(Props(new Actor { def receive = { case SendOne(runId, emailId) =>
        /*import context.system
        val future = Future { createMessage(runId, emailId) }
        pipe(future) to transport
        future onComplete { _ => context.stop(self) }*/
        val email = createMessage(runId, emailId)
        storeIds(email, runId, emailId)
        timing ! createdEmail(runId, emailId)
        transport ! email
        context.stop(self)
      }})) ! SendOne(runId, emailId)
  }
}

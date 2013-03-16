package com.pongr.jetray

import akka.actor._
import akka.routing._
import akka.util.duration._
import java.util.UUID.{randomUUID => randomUuid}
import javax.mail._
import Repeater._
import TestRun._

object SimpleMain extends App {
  //val Array(from, to, host, port, user, password, times) = args
  val Array(from, to, host, port, user, password, times) = Array("from@gmail.com", "to@gmail.com", "smtp.gmail.com", "587", "user@gmail.com", "password", "6")
  val properties = System.getProperties()
  properties.put("mail.smtp.auth", "true")
  properties.put("mail.smtp.starttls.enable", "true")
  val session = Session.getInstance(properties)

  val runId = randomUuid.toString
  val system = ActorSystem("simple")
  val repeater = system.actorOf(Props[Repeater], "repeater")
  val timing = system.actorOf(Props(new Actor with ActorLogging { def receive = { case msg => log.debug(msg.toString)}}), "timing")
  val transport = system.actorOf(Props(new TransportActor(session, host, port.toInt, user, password, timing)).withRouter(SmallestMailboxRouter(3)), "transport") //strategy?
  val run = system.actorOf(Props(new TestRun(runId, new SimpleCreateMessage(session, from, to), transport, timing)), "run")
  repeater ! Repeat(0 seconds, 1 second, run, Send, times.toInt)
}

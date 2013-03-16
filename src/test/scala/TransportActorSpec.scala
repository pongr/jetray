package com.pongr.jetray

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions
import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import akka.util.duration._
import javax.mail._

class TransportActorSpec extends TestKit(ActorSystem("test")) with SpecificationLike with NoTimeConversions with ImplicitSender {
  sequential

  class BufferTransport(session: Session, urlname: URLName) extends Transport(session, urlname) {
    var buffer: Seq[(Message, Array[javax.mail.Address])] = Nil
    override def sendMessage(message: Message, addresses: Array[javax.mail.Address]) {
      buffer :+= (message, addresses)
    }
  }
  //TODO need a way to use BufferTransport in TransportActor...

  "The TranspotActor" should {
    "send messages to the Transport under normal operation" in {
      todo
    }

    "reconnect to the Transport and resend the message up to 3 times on SMTPSendFailedException" in {
      todo
    }

    "give up after trying to resend 3 times" in {
      todo
    }
  }

  step(system.shutdown())
}

package com.pongr.jetray

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions
import org.specs2.matcher.MatchResult
import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import akka.util.duration._
import javax.mail._
import javax.mail.internet._
import TestRun._
import Timing._

class TestRunSpec extends TestKit(ActorSystem("test")) with SpecificationLike with NoTimeConversions with ImplicitSender {
  sequential

  val runId = "run1"
  val session = Session.getInstance(System.getProperties)
  def createMessage(runId: String, emailId: Int) = {
    val message = new MimeMessage(session)
    message.setSubject(runId + "-" + emailId)
    message
  }

  def aJetrayMessage(emailId: Int): PartialFunction[AnyRef, MatchResult[_]] = { case m: MimeMessage => 
    m.getSubject must_== (runId + "-" + emailId)
  }

  def aCreatedEmailTiming(emailId: Int): PartialFunction[AnyRef, MatchResult[_]] = { case t: Timing => 
    t.runId must_== runId
    t.emailId must_== emailId
    t.name must_== CreatedEmail
    t.time must be_>(0l)
  }

  "The TestRun" should {
    "send emails to the timing and transport actors" in {
      val run = system.actorOf(Props(new TestRun(runId, createMessage, testActor, testActor)))

      run ! Send
      receiveOne(1 second) must beLike(aCreatedEmailTiming(1))
      receiveOne(1 second) must beLike(aJetrayMessage(1))

      run ! Send
      receiveOne(1 second) must beLike(aCreatedEmailTiming(2))
      receiveOne(1 second) must beLike(aJetrayMessage(2))

      run ! Send
      receiveOne(1 second) must beLike(aCreatedEmailTiming(3))
      receiveOne(1 second) must beLike(aJetrayMessage(3))
    }

    "store and retrieve runId and emailId" in {
      val m1 = createMessage(runId, 1)
      storeIds(m1, runId, 1)
      getIds(m1) must_== (runId, 1)

      val m11 = createMessage(runId, 11)
      storeIds(m11, runId, 11)
      getIds(m11) must_== (runId, 11)
    }
  }

  step(system.shutdown())
}

package com.pongr.jetray

import org.specs2.mutable.SpecificationLike
import org.specs2.time.NoTimeConversions
import akka.testkit.{TestKit, ImplicitSender}
import akka.actor._
import akka.util.duration._

class RepeaterSpec extends TestKit(ActorSystem("test")) with SpecificationLike with NoTimeConversions with ImplicitSender {
  sequential

  import Repeater._

  "The Repeater" should {
    "send the message 1 time" in {
      val repeater = system.actorOf(Props[Repeater])
      watch(repeater)
      repeater ! Repeat(0 millis, 10 millis, testActor, "hi", 1)
      receiveN(1, 1 second) must_== Seq("hi")
      receiveOne(1 second) must_== Terminated(repeater)
    }

    "send the message 2 times" in {
      val repeater = system.actorOf(Props[Repeater])
      watch(repeater)
      repeater ! Repeat(0 millis, 10 millis, testActor, "hi", 2)
      receiveN(2, 1 second) must_== Seq("hi", "hi")
      receiveOne(1 second) must_== Terminated(repeater)
    }

    "send the message 3 times" in {
      val repeater = system.actorOf(Props[Repeater])
      watch(repeater)
      repeater ! Repeat(0 millis, 10 millis, testActor, "hi", 3)
      receiveN(3, 1 second) must_== Seq("hi", "hi", "hi")
      receiveOne(1 second) must_== Terminated(repeater)
    }
  }

  step(system.shutdown())
}

package com.pongr.jetray

import akka.actor._
import akka.util.Duration

object Repeater {
  case class Repeat(initialDelay: Duration, frequency: Duration, receiver: ActorRef, message: Any, times: Int)
}

/** Repeatedly sends a message to another actor. Uses Scheduler.schedule() but only repeats up to a fixed number of times.
  * The example below sends the message to the receiver a fixed number of times, with initial delay and frequency.
  * 
  * {{{
  * repeater ! Repeat(initialDelay, frequency, receiver, message, times)
  * }}}
  */
class Repeater extends Actor with ActorLogging {
  import Repeater._
  var receiverOption: Option[ActorRef] = None
  var cancellableOption: Option[Cancellable] = None
  var count = 0
  var max = 0

  def started = receiverOption.isDefined && cancellableOption.isDefined && max > 0

  def receive = {
    case r @ Repeat(initialDelay, frequency, receiver, message, times) => 
      val cancellable = context.system.scheduler.schedule(initialDelay, frequency, self, message)
      receiverOption = Some(receiver)
      cancellableOption = Some(cancellable)
      max = times
      log.debug("Scheduled {}", r)

    case message if started => 
      if (count >= max) {
        cancellableOption foreach { _.cancel() }
        log.debug("Finished")
        context.stop(self)
      } else {
        receiverOption foreach { _ ! message }
        count += 1
        log.debug("Sent {} to {}, count={}, max={}", message, receiverOption, count, max)
      }
  }
}

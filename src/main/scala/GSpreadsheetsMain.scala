package com.pongr.jetray

import akka.actor._
import akka.routing._
import Timing._

object GSpreadsheetsMain extends App {
  //inputs
  val spreadsheetName = "Jetray Timings"
  val runId = "2013-03-18T23:00:00"
  val count = 100
  val username = ""
  val password = ""

  println("Running...")

  println(GoogleSpreadsheetTimingStorage.createWorksheet(spreadsheetName, runId, count, Timings, username, password))

  val system = ActorSystem("gspread")
  val actor = system.actorOf(Props(new GoogleSpreadsheetTimingStorage(spreadsheetName, runId, Timings, username, password)).withRouter(SmallestMailboxRouter(3)))
  for (emailId <- (1 to 3)) {
    val t1 = System.currentTimeMillis
    actor ! createdEmail(runId, emailId, t1)
    actor ! preSentEmail(runId, emailId, t1 + 10)
    actor ! postSentEmail(runId, emailId, t1 + 20)
    Thread.sleep(500)
  }

  val runId2 = "2013-03-18T23:00:01"
  println(GoogleSpreadsheetTimingStorage.createWorksheet(spreadsheetName, runId2, count, Timings, username, password))
  for (emailId <- (1 to 3)) {
    val t1 = System.currentTimeMillis
    actor ! createdEmail(runId2, emailId, t1)
    actor ! preSentEmail(runId2, emailId, t1 + 10)
    actor ! postSentEmail(runId2, emailId, t1 + 20)
    Thread.sleep(500)
  }

  println("Done")

  //Thread.sleep(3000)
  //system.shutdown()
}

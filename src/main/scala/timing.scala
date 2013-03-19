package com.pongr.jetray

case class Timing(runId: String, emailId: Int, name: String, time: Long)

object Timing {
  val CreatedEmail = "CreatedEmail"
  def createdEmail(runId: String, emailId: Int, time: Long = System.currentTimeMillis) = Timing(runId, emailId, CreatedEmail, time)

  val PreSentEmail = "PreSentEmail"
  def preSentEmail(runId: String, emailId: Int, time: Long = System.currentTimeMillis) = Timing(runId, emailId, PreSentEmail, time)

  val PostSentEmail = "PostSentEmail"
  def postSentEmail(runId: String, emailId: Int, time: Long = System.currentTimeMillis) = Timing(runId, emailId, PostSentEmail, time)

  val SentResponse = "SentResponse"
  def sentResponse(runId: String, emailId: Int, time: Long = System.currentTimeMillis) = Timing(runId, emailId, SentResponse, time)

  val DenormalizedEvents = "DenormalizedEvents"
  def denormalizedEvents(runId: String, emailId: Int, time: Long = System.currentTimeMillis) = Timing(runId, emailId, DenormalizedEvents, time)

  val Timings = Seq(CreatedEmail, PreSentEmail, PostSentEmail, SentResponse, DenormalizedEvents)
}

/*
Google Spreadsheet timing storage
https://developers.google.com/google-apps/spreadsheets/#introduction
*/

import akka.actor._
import java.net.URL
import com.google.gdata.data.spreadsheet.{SpreadsheetEntry, SpreadsheetFeed, WorksheetFeed, WorksheetEntry, CellFeed, CellEntry}
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.PlainTextConstruct
import scala.collection.JavaConversions._

object GoogleSpreadsheetTimingStorage {

  val SpreadsheetFeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  def newSpreadsheetService(username: String, password: String, name: String = "Jetray"): SpreadsheetService = {
    val service = new SpreadsheetService(name)
    service.setUserCredentials(username, password)
    service
  }

  def spreadsheet(service: SpreadsheetService, name: String): Either[Exception, SpreadsheetEntry] = try {
    val serviceOption = service.getFeed(SpreadsheetFeedUrl, classOf[SpreadsheetFeed]).getEntries.find(_.getTitle.getPlainText == name)
    serviceOption.toRight(new NoSuchElementException("No spreadsheet with title '%s'" format name))
  } catch {
    case e: Exception => Left(e)
  }

  def worksheet(service: SpreadsheetService, spreadsheet: SpreadsheetEntry, name: String): Either[Exception, WorksheetEntry] = try {
    val o = service.getFeed(spreadsheet.getWorksheetFeedUrl, classOf[WorksheetFeed]).getEntries.find(_.getTitle.getPlainText == name)
    o.toRight(new NoSuchElementException("No worksheet with title '%s'" format name))
  } catch {
    case e: Exception => Left(e)
  }

  /** Creates a new worksheet in the specified spreadsheet for this run and adds the timing names as column names in the first row. */
  def createWorksheet(
    spreadsheetName: String, 
    runId: String, 
    emailCount: Int, 
    timingNames: Seq[String],
    username: String,
    password: String
  ): Either[Exception, WorksheetEntry] = try {
    val service = newSpreadsheetService(username, password)
    spreadsheet(service, spreadsheetName).right map { spreadsheet =>
      //Add a worksheet for runId
      val localWorksheet = new WorksheetEntry()
      localWorksheet.setTitle(new PlainTextConstruct(runId))
      localWorksheet.setColCount(timingNames.size)
      localWorksheet.setRowCount(emailCount + 1)
      val worksheet = service.insert(spreadsheet.getWorksheetFeedUrl, localWorksheet)

      //Insert the column names into first row
      val cellFeed = service.getFeed(worksheet.getCellFeedUrl, classOf[CellFeed])
      for ((name, i) <- timingNames.zipWithIndex) cellFeed.insert(new CellEntry(1, i + 1, name))

      worksheet
    }
  } catch {
    case e: Exception => Left(e)
  }
}

/** Stores timing data into a Google Spreadsheet. */
class GoogleSpreadsheetTimingStorage(
  spreadsheetName: String,
  runId: String,
  timingNames: Seq[String],
  username: String,
  password: String
) extends Actor with ActorLogging {
  import GoogleSpreadsheetTimingStorage._

  var lastRunId = runId
  var cellFeedOption: Option[CellFeed] = None
  def createCellFeed(runId: String) {
    val service = newSpreadsheetService(username, password)
    val cellFeedEither = for { //this is a pretty gross way to populate cellFeedOption...
      s <- spreadsheet(service, spreadsheetName).right
      w <- worksheet(service, s, runId).right
    } yield service.getFeed(w.getCellFeedUrl, classOf[CellFeed])
    cellFeedEither match {
      case Right(cellFeed) => 
        cellFeedOption = Some(cellFeed)
        lastRunId = runId
        log.debug("Obtained a CellFeed for worksheet {}/{}", spreadsheetName, runId)
      case Left(e) => throw e
    }
  }

  override def preStart() {
    createCellFeed(runId)
  }

  //TODO if these events come in at a high volume, we should batch them together instead of making singular http requests
  def receive = {
    case Timing(runId, emailId, name, time) => 
      if (runId != lastRunId) createCellFeed(runId)
      val row = emailId + 1
      val col = (timingNames indexOf name) + 1
      cellFeedOption foreach { _.insert(new CellEntry(row, col, time.toString)) }
  }

}

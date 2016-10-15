package services

import java.util.UUID

import com.websudos.phantom.dsl._
import models.{ConcreteSolertEntry, SolertEntry}
import play.api.Logger

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Random


private object Defaults {
  val connector = ContactPoint.apply("localhost", 9042).keySpace("solert")
}

class SolertService(val keyspace: KeySpaceDef) extends Database(keyspace) {

  object data extends ConcreteSolertEntry with keyspace.Connector

  def getById(location: UUID): Future[Option[SolertEntry]] = data.getById(location)

  def getEntriesNext3Hours(location: UUID): Future[Seq[SolertEntry]] = data.getFutureEntries(location, 3)

  def getEntriesNext24Hours(location: UUID): Future[Seq[SolertEntry]] = data.getFutureEntries(location, 24)

  def generateData(uuid: UUID) = {
    Await.ready(data.create.ifNotExists().future(), Duration.Inf)

    if (Await.result(getEntriesNext3Hours(uuid), Duration.Inf).isEmpty) {
      Logger.info("Generate data for "+uuid.toString)
      for (
        counter <- 0 to 1440;
        if counter % 15 == 0
      ) {
        Await.result(
          data.store(SolertEntry(uuid, Random.nextInt(100), new DateTime().plusMinutes(counter))),
          Duration.Inf
        )
      }
    }
  }
}

object SolertServiceImpl extends SolertService(Defaults.connector)
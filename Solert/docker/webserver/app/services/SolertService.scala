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

  object forecast extends ConcreteSolertEntry with keyspace.Connector

  def getById(location: String): Future[Option[SolertEntry]] = forecast.getById(location)

  def getEntriesNext3Hours(location: String): Future[Seq[SolertEntry]] = forecast.getFutureEntries(location, 150)

  def getEntriesNext24Hours(location: String): Future[Seq[SolertEntry]] = forecast.getFutureEntries(location, 1440)

  def generateData(location: String) = {
    Await.ready(forecast.create.ifNotExists().future(), Duration.Inf)

    if (Await.result(getEntriesNext3Hours(location), Duration.Inf).isEmpty) {
      Logger.info("Generate data for " + location.toString)
      for (
        counter <- 0 to 1440;
        if counter % 15 == 0
      ) {
        Await.result(
          forecast.store(SolertEntry(location, Random.nextInt(100), new DateTime().plusMinutes(counter))),
          Duration.Inf
        )
      }
    }
  }
}

object SolertServiceImpl extends SolertService(Defaults.connector)
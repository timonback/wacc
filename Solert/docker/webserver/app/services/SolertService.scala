package services

import com.websudos.phantom.dsl._
import models.{ConcreteSolertEntry, SolertEntry}
import play.api.Logger

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Random


private object Defaults {
  def dbAddress = sys.env.get("CASSANDRA_ADDRESS").getOrElse("localhost")
  def dbPort = sys.env.get("CASSANDRA_PORT").getOrElse("9042").toInt
  def dbKeySpace = sys.env.get("CASSANDRA_KEYSPACE").getOrElse("solert")
  val connector = ContactPoint.apply(dbAddress, dbPort).keySpace(dbKeySpace)
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
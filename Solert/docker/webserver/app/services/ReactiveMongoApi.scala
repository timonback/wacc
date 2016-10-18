package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{MongoDriver, MongoConnection}

object MongoDB {
  def dbAddress = sys.env.get("MONGO_ADDRESS").getOrElse("localhost")
  def dbPort = sys.env.get("MONGO_PORT").getOrElse("9042").toInt
  def dbKeySpace = sys.env.get("MONGO_KEYSPACE").getOrElse("solert")

  val mongoUri = "mongodb://" + dbAddress + ":" + dbPort + "/" + dbKeySpace;

  val driver = new MongoDriver

  val database = for {
    uri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    con = driver.connection(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield db

  database.onComplete {
    case resolution =>
      println(s"DB resolution: $resolution")
  }

  def shutDown = {
    driver.close()
  }
}
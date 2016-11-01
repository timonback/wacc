package services

import java.net.InetAddress

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.{MongoConnection, MongoDriver}

object MongoDB {
  def dbAddress = sys.env.get("MONGO_ADDRESS").getOrElse("localhost")
  def dbPort = sys.env.get("MONGO_PORT").getOrElse("27017").toInt
  def dbKeySpace = sys.env.get("MONGO_KEYSPACE").getOrElse("solert")

  val mongoUri = "mongodb://" + dbAddress + ":" + dbPort + "/" + dbKeySpace;
  println("Connecting to Mongo "+mongoUri)

  val driver = new MongoDriver

  val ips: Array[InetAddress] = InetAddress.getAllByName(mongoUri)

  val uris: Seq[String] = ips.map(ip => buildMongoUri(ip.getHostAddress, dbPort))

  val database = {
    val con = driver.connection(uris)
    con.database(dbKeySpace)
  }

  database.onComplete {
    case resolution =>
      println(s"DB resolution: $resolution")
  }

  def shutDown = {
    driver.close()
  }

  def buildMongoUri(dbAddress: String, dbPort : Int): String = {
    "mongodb://"+dbAddress + ":" + dbPort
  }
}

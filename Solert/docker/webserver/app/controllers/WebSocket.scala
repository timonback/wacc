package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import java.text.SimpleDateFormat
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import models.SolertEntry
import org.joda.time.DateTime
import play.Logger
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.api.mvc.{Controller, WebSocket}
import services.SolertServiceImpl

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.Duration
import scala.util.Random


class WebSocket @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
  val SolertService = SolertServiceImpl

  def weather = WebSocket.accept[JsValue, JsValue] { request =>
    Flow.fromFunction { in: JsValue =>
      Logger.info(in.toString)
      val location = (in \ "location").as[String]
      val lat = (in \ "lat").as[String]
      val lng = (in \ "lng").as[String]

      def uuid = UUID.fromString("d2a2f98e-c2d3-4256-8bb6-1059248a15ea")
      SolertServiceImpl.generateData(uuid)

      def hours3 = for {
        dataSeq <- SolertServiceImpl.getEntriesNext3Hours(uuid)
        result <- Promise.successful(dataSeq.map { entry =>
          Json.obj(
            "time" -> entry.time,
            "value" -> entry.value
          )
        }.toList).future
      } yield result

      def hours24 = for {
        dataSeq <- SolertServiceImpl.getEntriesNext24Hours(uuid)
        result <- Promise.successful(dataSeq.map { entry =>
          Json.obj(
            "time" -> entry.time,
            "value" -> entry.value
          )
        }.toList).future
      } yield result

      Json.obj(
        "original-request" -> in,
        "hours3" -> Json.toJson(Await.result(hours3, Duration.fromNanos(1000000000))),
        "hours24" -> Json.toJson(Await.result(hours24, Duration.fromNanos(1000000000)))
      )
    }
  }
}

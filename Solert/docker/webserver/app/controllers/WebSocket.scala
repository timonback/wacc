package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import play.Logger
import play.api.libs.json._
import play.api.mvc.{Controller, WebSocket}
import services.SolertServiceImpl

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration


class WebSocket @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
  val SolertService = SolertServiceImpl

  def weather = WebSocket.accept[JsValue, JsValue] { request =>
    Flow.fromFunction { in: JsValue =>
      Logger.info(in.toString)
      val location = (in \ "location").asOpt[String].getOrElse("")

      //SolertServiceImpl.generateData(location)

      def hours3 = for {
        dataSeq <- SolertServiceImpl.getEntriesNext3Hours(location)
        result <- Promise.successful(dataSeq.map { entry =>
          Json.obj(
            "time" -> entry.datetime,
            "value" -> entry.value
          )
        }.toList).future
      } yield result

      def hours24 = for {
        dataSeq <- SolertServiceImpl.getEntriesNext24Hours(location)
        result <- Promise.successful(dataSeq.map { entry =>
          Json.obj(
            "time" -> entry.datetime,
            "value" -> entry.value
          )
        }.toList).future
      } yield result

      Json.obj(
        "original-request" -> in,
        "hours3" -> Json.toJson(Await.result(hours3, Duration.create(1, TimeUnit.SECONDS))),
        "hours24" -> Json.toJson(Await.result(hours24, Duration.create(1, TimeUnit.SECONDS)))
      )
    }
  }
}

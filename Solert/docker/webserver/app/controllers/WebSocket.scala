package controllers
import java.text.SimpleDateFormat
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}

import scala.util.Random

/**
  * CustomWebSocket is a controller which demo's some of the WebSocket functionality.
  *
  * @param system Implicit Akka system, injected by Play
  * @param materializer Implicit Akka materializer, injected by Play
  */
class WebSocket @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {

  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

  def weather = WebSocket.accept[JsValue, JsValue] { request => // This WebSocket accepts and returns JsValue (JSON objects)
    Flow.fromFunction { in =>
      Json.obj( // Construct a JSON object using Play Json
        "original-request" -> in,
        "time3H"       -> Json.arr(format.format(DateTime.now.toDate), format.format(DateTime.now.plusMinutes(15).toDate), format.format(DateTime.now.plusMinutes(30).toDate)),
        "values3H"     -> Json.toJson(Seq.fill(3)(Random.nextInt(100))),
        "time24H"      -> Json.arr(format.format(DateTime.now.toDate), format.format(DateTime.now.plusMinutes(15).toDate), format.format(DateTime.now.plusMinutes(30).toDate)),
        "values24H"    -> Json.toJson(Seq.fill(3)(Random.nextInt(100)))
      )
    }
  }
}

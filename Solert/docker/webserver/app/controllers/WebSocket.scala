package controllers
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.util.Random

/**
  * CustomWebSocket is a controller which demo's some of the WebSocket functionality.
  *
  * @param system Implicit Akka system, injected by Play
  * @param materializer Implicit Akka materializer, injected by Play
  */
class WebSocket @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller {

  /**
    * A simple web socket which echo's any message that it receives.
    *
    * @return The created WebSocket
    */
  def echo = WebSocket.accept[String, String] { request =>   // WebSocket.accept[Input, Output]
    // Create a Flow based on an actor
    ActorFlow.actorRef(out => WebSocketActor.props(out)) // Akka Actor based using CustomWebSocketActor, results in a Flow[Input, Output]
  }

  /**
    * A web socket that generates random integers whenever it receives a message.
    *
    * @return The created WebSocket.
    */
  def randomInteger = WebSocket.accept[String, String] { request =>
    // Create a flow based on a function
    Flow.fromFunction(in => Random.nextInt().toString) // Akka Stream based, create Flow[Input, Output] directly
  }

  /**
    * A web socket that generates a complex JSON object with random values.
    * @return The created WebSocket.
    */
  def complexJson = WebSocket.accept[JsValue, JsValue] { request => // This WebSocket accepts and returns JsValue (JSON objects)
    Flow.fromFunction { in =>
      Json.obj( // Construct a JSON object using Play Json
        "original-request" -> in,
        "some-string"  -> Random.nextString(10),
        "some-int"     -> Random.nextInt(),
        "some-boolean" -> Random.nextBoolean(),
        "some-double"  -> Random.nextDouble(),
        "some-object-list" -> Json.arr(
          Json.obj("another-string" -> Random.nextString(5)),
          Json.obj("another-string" -> Random.nextString(5)),
          Json.obj("another-string" -> Random.nextString(5))
        )
      )
    }
  }

  /**
    * Create the HTML page containing the WebSocket echo demo
    *
    * @return The resulting HTML page
    */
  def echoIndex = Action {
    // title = Title of the page
    // tab = tab-name, used to add "active" class to menu items
    // endpoint = the websocket endpoint to use for this demo, see Routes
    Ok(views.html.websocket(title = "Echo", tab = "ws-echo", endpoint = "/ws/echo"))
  }

  /**
    * Create the HTML page containing the WebSocket random integer demo
    *
    * @return The resulting HTML page
    */
  def randomIntegerIndex() = Action {
    Ok(views.html.websocket("Random Integer", "ws-rand", "/ws/rand"))
  }

  /**
    * Create the HTML page containing the WebSocket complex JSON demo
    *
    * @return The resulting HTML page
    */
  def complexJsonIndex() = Action {
    Ok(views.html.websocket("Complex JSON", "ws-complex", "/ws/complex"))
  }
}

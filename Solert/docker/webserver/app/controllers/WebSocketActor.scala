package controllers

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

/**
  * Singleton object to facilitate the creation of new Actors
  */
object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

/**
  * Custom actor that echo's messages that it receives.
  *
  * @param out Reference to the Actor to which a reply can be sent
  */
class WebSocketActor(out: ActorRef) extends Actor {
  Logger.info("Created WebSocket Actor")

  def receive = {
    case msg: String => // If the message received is a String...
      Logger.info(s"Received $msg, echoing")
      out ! msg // Send reply to output Actor
    case x => Logger.warn(s"Unknown message $x") // The message was something other than a String
  }

  override def postStop() = {
    Logger.info("WebSocket has closed")
  }
}
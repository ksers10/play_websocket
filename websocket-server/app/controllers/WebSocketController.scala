package controllers

import javax.inject.Inject

import actors.WebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}

/**
  * The WebSocket is created here.
  *
  * @param system implicit ActorSystem
  * @param materializer implicit Materializer for Akka Actors.
  */

class WebSocketController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller{

  def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { implicit request =>
    // Upstream messages are sent to the WebSocketActor that acts as a router for privileged and non-privileged requests.
    ActorFlow.actorRef(out => WebSocketActor.props(out, request.session))
  }

}

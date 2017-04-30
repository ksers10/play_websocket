package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json._
import play.api.mvc.Session


object WebSocketActor {
  def props(out: ActorRef, requestSession: Session): Props = Props(new WebSocketActor(out, requestSession))
}

/**
  * Akka Actor that acts as router between privileged and non-privileged requests.
  * It creates a child Actor for each type and forwards the incoming message to it.
  *
  * @param out ActorRef for message passing back to the Client
  * @param requestSession initial session value
  */
class WebSocketActor(out: ActorRef, requestSession: Session) extends Actor {

  // The session is stored in a variable, because it can be updated with a user type after successful login.
  private var _userSession = requestSession

  private val _nonPrivilegedRequest = Set("login", "ping", "subscribe_tables", "unsubscribe_tables")
  private val _privilegedRequest = Set("add_table", "update_table", "remove_table")

  lazy val nonPrivilegedActor: ActorRef = context.actorOf(NonPrivilegedActor.props(requestSession, out), name = "NonPrivilegedActor")
  lazy val privilegedActor: ActorRef = context.actorOf(PrivilegedActor.props(out), name = "PrivilegedActor")

  def receive = {
    case sessionUpdate: Session =>
      // Session updates can come from child NonPrivilegedActor.
      _userSession = sessionUpdate
    case msg: JsValue =>
      (msg \ "$type").validate[String] match {
        case requestType: JsSuccess[String] if _nonPrivilegedRequest.contains(requestType.get) =>
          nonPrivilegedActor ! (msg, requestType.get)
        case requestType: JsSuccess[String] if _privilegedRequest.contains(requestType.get) =>
          val userType: String = _userSession.get("usertype") getOrElse ""
          if (userType == "admin") {
            privilegedActor ! (msg, requestType.get)
          } else {
            out ! Json.obj("$type" -> "not_authorized")
          }
        case _ =>
          out ! Json.obj("error" -> "unsupported request")
      }
  }

}

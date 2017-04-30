package actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import models.{Table, Tables, Users}
import play.api.libs.json._
import play.api.mvc.Session

case class Login($type: String, username: String, password: String)

case class Ping($type: String, seq: Short)

object NonPrivilegedActor {
  def props(requestSession: Session, out: ActorRef): Props = Props(new NonPrivilegedActor(requestSession, out))
}

/**
  * Akka Actor that handles requests that do not require authentication.
  * Login requests are also handled here.
  *
  * @param requestSession current session value.
  * @param out ActorRef for message passing back to the Client.
  */
class NonPrivilegedActor(requestSession: Session, out: ActorRef) extends Actor {

  // Implicit values for mapping Json to case classes.
  private implicit val _loginReads = Json.reads[Login]
  private implicit val _pingReads = Json.format[Ping]
  private implicit val _tablesWrites = Json.writes[Table]

  lazy val subscriber: ActorRef = context.actorOf(SubscriberActor.props(out))

  def receive = {
    case (json: JsValue, requestType: String) =>
      requestType match {
        case "login" =>
          json.validate[Login] match {
            case JsSuccess(login: Login, _: JsPath) =>
              Users.authenticate(login.username, login.password) match {
                case Right(userType) => {
                  // Update the session with user type if authentication & authorization were successful.
                  sender() ! requestSession + ("usertype" -> userType)
                  out ! Json.obj("$type" -> "login_successful", "user_type" -> userType)
                }
                case Left(failure) => out ! Json.obj("$type" -> failure)
              }
            case e: JsError => out ! JsError.toJson(e)
          }
        case "ping" =>
          json.validate[Ping] match {
            case JsSuccess(ping: Ping, _: JsPath) =>
              out ! Json.toJson(Ping("pong", ping.seq))
            case e: JsError => out ! JsError.toJson(e)
          }
        case "subscribe_tables" =>
          val tables = Json.toJson(Tables.tables)
          out ! Json.obj("$type" -> "table_list", "tables" -> tables)
          /** Subscribe to tables' updated by starting the SubscriberActor
            * that listens to the "tables_updates" topic.
            */
          subscriber
        case "unsubscribe_tables" =>
          // Cancels subscription by stopping the subscribing Actor.
          subscriber ! PoisonPill
      }
  }

}

package actors

import akka.actor.{Actor, ActorRef, Props}
import models.{Table, Tables}
import play.api.libs.json._

case class AddTable($type: String, after_id: Int, table: Table)

case class UpdateTable($type: String, table: Table)

case class RemoveTable($type: String, id: Int)

object PrivilegedActor {
  def props(out: ActorRef): Props = Props(new PrivilegedActor(out))
}

/**
  * Akka Actor that handles requests that require authentication.
  * Handling these requests successfully imply sending updates to the subscribers.
  * Therefore this Actor sends messages both back to the Client and to the PublisherActor.
  *
  * @param out ActorRef for message passing back to the client.
  */
class PrivilegedActor(out: ActorRef) extends Actor {

  // Implicit values for mapping Json to case classes.
  private implicit val _tableFormat = Json.format[Table]
  private implicit val _addTableFormat = Json.format[AddTable]
  private implicit val _updateTableFormat = Json.format[UpdateTable]
  private implicit val _removeTableFormat = Json.format[RemoveTable]

  val publisher: ActorRef = context.actorOf(PublisherActor.props)

  def receive = {
    case (json: JsValue, requestType: String) =>
      requestType match {
        case "add_table" =>
          json.validate[AddTable] match {
            case JsSuccess(addTable: AddTable, _: JsPath) =>
              Tables.add(addTable.after_id, addTable.table) match {
                case Right(msgOK) => {
                  val response = Json.toJson(AddTable(msgOK, addTable.after_id, addTable.table))
                  out ! response
                  publisher ! response
                }
                case Left(msgKO) => out ! Json.obj("$type" -> msgKO)
              }
            case e: JsError => out ! JsError.toJson(e)
          }
        case "update_table" =>
          json.validate[UpdateTable] match {
            case JsSuccess(updateTable: UpdateTable, _: JsPath) =>
              Tables.update(updateTable.table) match {
                case Right(msgOK) => {
                  val response = Json.toJson(UpdateTable(msgOK, updateTable.table))
                  out ! response
                  publisher ! response
                }
                case Left(msgKO) => out ! Json.obj("$type" -> msgKO, "id" -> updateTable.table.id)
              }
            case e: JsError => out ! JsError.toJson(e)
          }
        case "remove_table" =>
          json.validate[RemoveTable] match {
            case JsSuccess(removeTable: RemoveTable, _: JsPath) =>
              Tables.remove(removeTable.id) match {
                case Right(msgOK) => {
                  val response = Json.toJson(RemoveTable(msgOK, removeTable.id))
                  out ! response
                  publisher ! response
                }
                case Left(msgKO) => out ! Json.toJson(RemoveTable(msgKO, removeTable.id))
              }
            case e: JsError => out ! JsError.toJson(e)
          }
      }
  }

}

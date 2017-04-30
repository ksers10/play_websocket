package app

import java.net.URI

import io.backchat.hookup.{DefaultHookupClient, HookupClientConfig, JsonMessage}
import org.json4s.jackson.JsonMethods.{pretty, render}
import org.specs2.mutable.Specification
import play.api.test.WithServer

import scala.collection.mutable.ListBuffer

/**
  * These are not actual Unit tests but rather an emulation of
  * interaction of Clients with the WebSocket server.
  *
  * A test server is started by Play framework.
  * WebSocket clients are created with the help of backhat 3rd party library.
  * Clients send requests to the actual implemented WebSocket server.
  *
  * Clients send different types of requests
  * and responses from the server are evaluated with Specs.
  *
  * This test tries to cover all types of requests a Client can send to the server
  * and all types of responses from the server.
  */
class WebSocketSpec extends Specification {

  import WebSocketSpec._

  "These messages to WebSocket server" should {

    "test it's API" in new WithServer(port = 9000) {

      val client = hookupClient("client")
      val subscribingClient1 = hookupClient("subscriber1")
      val subscribingClient2 = hookupClient("subscriber2")

      println("Subscribing client 1 sends:\n" + subscribeTables)
      subscribingClient1.send(subscribeTables)
      subClient1_ServerResponse.contains(
        """{
          |  "$type" : "table_list",
          |  "tables" : [ ]
          |}""".stripMargin) must beTrue.eventually
      subClient1_ServerResponse.clear()
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      client_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Subscribing client 2 sends:\n" + subscribeTables)
      subscribingClient2.send(subscribeTables)
      subClient2_ServerResponse.contains(
        """{
          |  "$type" : "table_list",
          |  "tables" : [ ]
          |}""".stripMargin) must beTrue.eventually
      subClient2_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      client_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + ping)
      client.send(ping)
      client_ServerResponse.contains(
        """{
          |  "$type" : "pong",
          |  "seq" : 1
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + wrongCredentials)
      client.send(wrongCredentials)
      client_ServerResponse.contains(
        """{
          |  "$type" : "login_failed"
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + addTable)
      client.send(addTable)
      client_ServerResponse.contains(
        """{
          |  "$type" : "not_authorized"
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + userLogin)
      client.send(userLogin)
      client_ServerResponse.contains(
        """{
          |  "$type" : "login_successful",
          |  "user_type" : "user"
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + addTable)
      client.send(addTable)
      client_ServerResponse.contains(
        """{
          |  "$type" : "not_authorized"
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + adminLogin)
      client.send(adminLogin)
      client_ServerResponse.contains(
        """{
          |  "$type" : "login_successful",
          |  "user_type" : "admin"
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + addTable)
      client.send(addTable)
      client_ServerResponse.contains(
        """{
          |  "$type" : "table_added",
          |  "after_id" : -1,
          |  "table" : {
          |    "name" : "table - Initial",
          |    "participants" : 1
          |  }
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.contains(
        """{
          |  "$type" : "table_added",
          |  "after_id" : -1,
          |  "table" : {
          |    "name" : "table - Initial",
          |    "participants" : 1
          |  }
          |}""".stripMargin) must beTrue.eventually
      subClient1_ServerResponse.clear()
      subClient2_ServerResponse.contains(
        """{
          |  "$type" : "table_added",
          |  "after_id" : -1,
          |  "table" : {
          |    "name" : "table - Initial",
          |    "participants" : 1
          |  }
          |}""".stripMargin) must beTrue.eventually
      subClient2_ServerResponse.clear()
      Thread.sleep(2000)

      println("Subscribing client 2 sends:\n" + unsubscribeTables)
      subscribingClient2.send(unsubscribeTables)
      client_ServerResponse.isEmpty must beTrue.eventually
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + updateTableFail)
      client.send(updateTableFail)
      client_ServerResponse.contains(
        """{
          |  "$type" : "update_failed",
          |  "id" : 10
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + updateTable)
      client.send(updateTable)
      client_ServerResponse.contains(
        """{
          |  "$type" : "table_updated",
          |  "table" : {
          |    "id" : 0,
          |    "name" : "table - Foo Fighters",
          |    "participants" : 4
          |  }
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.contains(
        """{
          |  "$type" : "table_updated",
          |  "table" : {
          |    "id" : 0,
          |    "name" : "table - Foo Fighters",
          |    "participants" : 4
          |  }
          |}""".stripMargin) must beTrue.eventually
      subClient1_ServerResponse.clear()
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + removeTableFail)
      client.send(removeTableFail)
      client_ServerResponse.contains(
        """{
          |  "$type" : "removal_failed",
          |  "id" : 10
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.isEmpty must beTrue.eventually
      subClient2_ServerResponse.isEmpty must beTrue.eventually
      Thread.sleep(2000)

      println("Client sends:\n" + removeTable)
      client.send(removeTable)
      client_ServerResponse.contains(
        """{
          |  "$type" : "table_removed",
          |  "id" : 0
          |}""".stripMargin) must beTrue.eventually
      client_ServerResponse.clear()
      subClient1_ServerResponse.contains(
        """{
          |  "$type" : "table_removed",
          |  "id" : 0
          |}""".stripMargin) must beTrue.eventually
      subClient1_ServerResponse.clear()
      subClient2_ServerResponse.isEmpty must beTrue.eventually

    }
  }

}

object WebSocketSpec {
  val client_ServerResponse = ListBuffer[String]()
  val subClient1_ServerResponse = ListBuffer[String]()
  val subClient2_ServerResponse = ListBuffer[String]()

  def hookupClient(sender: String) = {
    val client = new DefaultHookupClient(HookupClientConfig(URI.create("ws://localhost:9000/ws"))) {

      def receive = {

        case JsonMessage(json) =>
          val prettyjson = pretty(render(json))
          if (sender == "client") {
            client_ServerResponse += prettyjson
            println("Server responds to Client:\n" + prettyjson)
          } else if (sender == "subscriber1") {
            subClient1_ServerResponse += prettyjson
            println("Server responds to Subscribing client 1:\n" + prettyjson)
          } else {
            subClient2_ServerResponse += prettyjson
            println("Server responds to Subscribing client 2:\n" + prettyjson)
          }
      }

      connect()
    }
    client
  }

  val ping =
    """{
      |  "$type": "ping",
      |  "seq": 1
      |}""".stripMargin
  val wrongCredentials =
    """{
      |  "$type": "login",
      |  "username": "user1234",
      |  "password": "wrongpassword"
      |}""".stripMargin
  val adminLogin =
    """{
      |  "$type": "login",
      |  "username": "admin",
      |  "password": "secretpassword"
      |}""".stripMargin
  val userLogin =
    """{
      |  "$type": "login",
      |  "username": "user1234",
      |  "password": "password1234"
      |}""".stripMargin
  val subscribeTables =
    """{
      |  "$type": "subscribe_tables"
      |}""".stripMargin
  val unsubscribeTables =
    """{
      |  "$type": "unsubscribe_tables"
      |}""".stripMargin
  val addTable =
    """{
      |  "$type": "add_table",
      |  "after_id": -1,
      |  "table": {
      |    "name": "table - Initial",
      |    "participants": 1
      |  }
      |}""".stripMargin
  val updateTable =
    """{
      |  "$type": "update_table",
      |  "table": {
      |    "id": 0,
      |    "name": "table - Foo Fighters",
      |    "participants": 4
      |  }
      |}""".stripMargin
  val updateTableFail =
    """{
      |  "$type": "update_table",
      |  "table": {
      |    "id": 10,
      |    "name": "table - Foo Fighters",
      |    "participants": 4
      |  }
      |}""".stripMargin
  val removeTable =
    """{
      |  "$type": "remove_table",
      |  "id": 0
      |}""".stripMargin
  val removeTableFail =
    """{
      |  "$type": "remove_table",
      |  "id": 10
      |}""".stripMargin

}

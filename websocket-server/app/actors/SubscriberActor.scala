package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import play.api.libs.json.JsValue

object SubscriberActor {
  def props(out: ActorRef): Props = Props(new SubscriberActor(out))
}

/**
  * Akka Actor that subscribes to the "tables_updates" topic and
  * sends updates back to the Client.
  *
  * @param out ActorRef for message passing back to the client.
  */
class SubscriberActor(out: ActorRef) extends Actor {

  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  mediator ! Subscribe("tables_updates", self)

  def receive = {
    case update: JsValue ⇒
      out ! update
  }

}

package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import play.api.libs.json.JsValue

object PublisherActor {
  def props: Props = Props(new PublisherActor)
}

/**
  * Akka Actor that publishes messages to the "tables_updates" topic.
  * Messages come from the PrivilegedActor.
  */
class PublisherActor extends Actor {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def receive = {
    case update: JsValue =>
      mediator ! Publish("tables_updates", update)
  }

}

package notifications


import common.actors._
import common.messages._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import play.api.libs.concurrent.Akka._
import application.messages._
import application.workers._
import play.api.Play
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current
import persistence._

class NotificationsProxy(
  system: ActorSystem,
) extends Actor with Master[WazzaMessage, MailWorker] {

  private val NUMBER_WORKERS = 1

  override def workersRouter = {
    val routees = Vector.fill(NUMBER_WORKERS) {
      val r = context.actorOf(MailWorker.props)
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def killRouter = {}

  protected def execute[MailRequest](request: MailRequest) = {
    workersRouter.route(request, sender())
  }

  def receive = masterReceive
}

object NotificationsProxy {

  private var singleton: ActorRef = null

  def getInstance = {
    if(singleton == null) {
      singleton = Akka.system.actorOf(
        NotificationsProxy.props(ActorSystem("notifications")), name = "notifications"
      )
    }
    singleton
  }

  def props(system: ActorSystem,): Props = Props(new NotificationsProxy(system))
}


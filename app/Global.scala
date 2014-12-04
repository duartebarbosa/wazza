import play.api.{GlobalSettings, Application}
import com.google.inject._
import play.api._
import play.api.Play
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import service.application.modules._
import service.user.definitions._
import service.user.implementations._
import service.user.modules._
import service.security.modules._
import service.aws.modules._
import service.persistence.modules.PersistenceModule
import service.analytics.modules.AnalyticsModule
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import akka.actor.{ActorRef, Actor, ActorSystem, Kill, Props}
import persistence._
import application._
import user._
import notifications.plugins._
import java.io.{StringWriter, PrintWriter}  
import scala.concurrent._
import scala.concurrent.duration._

object Global extends GlobalSettings with MailConnector {

  private var modulesProxies = List[ActorRef]()

  /**
    Creates modules system's and proxies
  **/
  override def onStart(app: Application) = {
    val databaseProxy = PersistenceProxy.getInstance
    val userProxy = UserProxy.getInstance
    val applicationProxy = ApplicationProxy.getInstance
    modulesProxies = List(databaseProxy, userProxy, applicationProxy)
  }

  // 500 - internal server error
  override def onError(request: RequestHeader, throwable: Throwable) = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    throwable.printStackTrace(pw)
    val stack = sw.toString()
    val msg = s"Message: ${throwable.getMessage}\nStack Trace: ${stack}"
    MailProxy.sendEmail("500 ERROR", List("support@wazza.io"), msg)
    Future.successful(InternalServerError(views.html.errorPage()))
  }
  /**
    Shutdowns all modules' systems and actors
  **/
  override def onStop(app: Application) = modulesProxies.foreach{_ ! Kill}

  /**
    Dependency injection setup
  **/
  private lazy val injector = {
    Guice.createInjector(
      new PersistenceModule,
      new AppModule,
      new UserModule,
      new SecurityModule,
      new AWSModule,
      new PersistenceModule,
      new AnalyticsModule
    )
  }

  override def getControllerInstance[A](clazz: Class[A]) = {
    injector.getInstance(clazz)
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    val msg = s"Trying to access path: ${request.path}"
    MailProxy.sendEmail("404 ERROR", List("support@wazza.io"), msg)
    Future.successful(NotFound(
      views.html.index()
    ))
  }
}


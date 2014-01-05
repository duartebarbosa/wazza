package controllers.application

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models.application._
import com.google.inject._
import service.application.definitions._
import play.api.data.format.Formats._
/** Uncomment the following lines as needed **/
/**
import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

class ItemCRUDController @Inject()(
    applicationService: ApplicationService
  ) extends Controller {

  // val googlePlayForm: Form[Item] = Form(
  //   mapping(
  //     "name" -> nonEmptyText,
  //     "description" -> nonEmptyText,
  //     "store" -> number,
  //     "metadata" -> mapping(
  //       "osType" -> ignored("Android"),
  //       "itemId" -> nonEmptyText,
  //       "title" -> nonEmptyText,
  //       "description" -> ignored(""),
  //       "publishedState" -> ignored(""),
  //       "purchaseType" -> number,
  //       "autoTranslate" -> ignored(false),
  //       "locale" -> ignored(List[GoogleTranslations]()),
  //       "autofill" -> ignored(false),
  //       "language" -> nonEmptyText,
  //       "price" -> of[Double]
  //     )(GoogleMetadata.apply)(GoogleMetadata.unapply),
  //     "currency" -> mapping(
  //       "typeOf" -> number,
  //       "value" -> of[Double]
  //     )(Currency.apply)(Currency.unapply)
  //   )
  // )(Item.apply)(Item.unapply)
  
  def newItem(storeType: String) = Action { implicit request =>
    if(applicationService.getApplicationyTypes.contains(storeType)){
      Ok(views.html.newItem(storeType, List("Real", "Virtual")))
    } else {
      BadRequest("Unknown store type")
    }

  }

  def newItemSubmit(applicationName: String) = TODO
}
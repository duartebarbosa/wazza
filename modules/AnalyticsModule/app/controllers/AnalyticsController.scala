package controllers.analytics

import com.google.inject._
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import service.analytics.definitions.AnalyticsService

class AnalyticsController @Inject()(
  analyticsService: AnalyticsService
) extends Controller {

  private def validateDate(dateStr: String): Try[Date] = {
    val df = new SimpleDateFormat("dd-mm-yyyy")
    try {
      val date = df.parse(dateStr)
      new Success(date)
    } catch {
      case ex: ParseException => {
        new Failure(ex)
      }
    }
  }

  def getTotalRevenue(
    companyName: String,
    applicationName: String,
    startDateStr: String,
    endDateStr: String
  ) = Action {implicit request =>

    //val dates = (validateDate(startDateStr), validateDate(endDateStr))

    analyticsService.getTotalRevenue(companyName, applicationName, null, null)

    Ok
  }

  def test = TODO
}


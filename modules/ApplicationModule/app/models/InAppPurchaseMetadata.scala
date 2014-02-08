package models.application

import play.api.libs.json._
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import scala.reflect.runtime.universe._
import scala.language.implicitConversions

@Salat
trait InAppPurchaseMetadata {
  def osType: String
  def itemId: String
  def title: String
  def description: String
}

case class GoogleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  publishedState: String,
  purchaseType: String,
  autoTranslate: Boolean,
  locale: List[GoogleTranslations],
  autofill: Boolean,
  language: String,
  price: Double,
  countries: List[CountryInfo]

) extends InAppPurchaseMetadata

case class CountryInfo(
  name: String
)

case class GoogleTranslations(
  locale: String,
  title: String,
  description: String
)

case class AppleMetadata(
  override val osType: String,
  override val itemId: String,
  override val title: String,
  override val description: String,
  productProperties: AppleProductProperties,
  languageProperties: AppleLanguageProperties,
  pricingProperties: ApplePricingProperties,
  durationProperties: AppleDurationProperties

) extends InAppPurchaseMetadata

case class AppleProductProperties(
  productType: Int, 
  status: String,
  reviewNotes: String
  // screenshot: TODO
)

case class AppleLanguageProperties(
  language: String,
  display: String,
  description: String,
  publicationName: String
)

case class ApplePricingProperties(
  clearedForSale: Boolean,
  price: Double,
  pricingAvailability: PricingAvailability
)

case class PricingAvailability(
  begin: Date,
  end: Date
)

case class AppleDurationProperties(
  autoRenewalDuration: Date,
  freeTrialDuration: Date,
  marketingIncentiveDuration: Date
)

object InAppPurchaseMetadata {

  def buildJson(metadata: InAppPurchaseMetadata): JsValue = {
    metadata match {
      case google: GoogleMetadata => {
        Json.obj(
          "osType" -> google.osType,
          "itemId" -> google.itemId,
          "title" -> google.title,
          "description" -> google.description,
          "publicationName" -> google.publishedState,
          "purchaseType" -> google.purchaseType,
          "autoTranslate" -> google.autoTranslate,
          "locale" -> Json.toJson(google.locale.map((el: GoogleTranslations) => {
            Json.obj("locale" -> el.locale, "title" -> el.title, "description" -> el.description)
          })),
          "autofill" -> google.autofill,
          "language" -> google.language,
          "price" -> google.price
        )
      }
      case apple: AppleMetadata => {
        //TODO
        Json.obj()
      }
      case _ => null
    }
  }

  val LanguageCodes = Map(
    "Chinese" ->    "zh_TW",
    "Italian" ->    "it_IT",
    "Czech" ->      "cs_CZ",
    "Japanese" ->   "ja_JP",
    "Danish" ->     "da_DK",
    "Korean" ->     "ko_KR",
    "Dutch" ->      "nl_NL",
    "Norwegian" ->  "no_NO",
    "English" ->    "en_US",
    "Polish" ->     "pl_PL",
    "French" ->     "fr_FR",
    "Portuguese" -> "pt_PT",
    "Finnish" ->    "fi_FI",
    "Russian" ->    "ru_RU",
    "German" ->     "de_DE",
    "Spanish" ->    "es_ES",
    "Hebrew" ->     "iw_IL",
    "Swedish" ->    "sv_SE",
    "Hindi" ->      "hi_IN"
  )
}

package object InAppPurchaseContext {

  // Stores Info
  lazy val GoogleStoreId = 0
  lazy val AppleStoreId = 1
  lazy val GoogleMetadataType = "models.application.GoogleMetadata"
  lazy val AppleMetadataType = "models.application.AppleMetadata"

  // Currency Types
  lazy val VirtualCurrencyType = 0
  lazy val RealWordCurrencyType = 1

  // Purchase Types
  lazy val ManagedProduct = 0
  lazy val Subscription = 1
  lazy val UnManaged = 2

  implicit def jsonToMetadata(obj: JsValue): InAppPurchaseMetadata = {
    val metadataType = (obj \ "_t").as[String]
    if(metadataType == GoogleMetadataType){
      new GoogleMetadata(
        (obj \ "osType").as[String],
        (obj \ "itemId").as[String],
        (obj \ "title").as[String],
        (obj \ "description").as[String],
        (obj \ "publishedState").as[String],
        (obj \ "purchaseType").as[String],
        (obj \ "autoTranslate").as[Boolean],
        (obj \ "locale"),
        (obj \ "autofill").as[Boolean],
        (obj \ "language").as[String],
        (obj \ "price").as[Double],
        (obj \ "country")
      )
    } else {
      new AppleMetadata(
        (obj \ "osType").as[String],
        (obj \ "itemId").as[String],
        (obj \ "title").as[String],
        (obj \ "description").as[String],
        (obj \ "productProperties"),
        (obj \ "languageProperties"),
        (obj \ "pricingProperties"),
        (obj \ "languageProperties")
      )
    }
  }

  implicit def jsonToCurrency(obj: JsValue): Currency = {
    new Currency(
      (obj \ "typeOf").as[Int],
      (obj \ "value").as[Double],
      (obj \ "virtualCurrency").asOpt[String]
    )
  }

  implicit def jsonArrayToLocale(obj: JsValue): List[GoogleTranslations] = {
    obj match {
      case JsArray(array) => {
        array.map((element: JsValue) => {
          new GoogleTranslations(
              (element \ "locale").as[String],
              (element \ "title").as[String],
              (element \ "description").as[String]
          )
        }).toList
      }
      case _ => List[GoogleTranslations]()
    }
  }

  implicit def jsonArrayToCountryInfo(obj: JsValue): List[CountryInfo] = {
    obj match {
      case JsArray(array) => {
        array.map((el: JsValue) => {
          new CountryInfo(
            (el \ "name").as[String]
          )
        }).toList
      }
      case _ => List[CountryInfo]()
    }
  }

  implicit def jsonToAppleProductProperties(obj: JsValue): AppleProductProperties = {
    new AppleProductProperties(
      (obj \ "productType").as[Int],
      (obj \ "status").as[String],
      (obj \ "reviewNotes").as[String]
    )
  }

  implicit def jsonToAppleLanguageProperties(obj: JsValue): AppleLanguageProperties = {
    new AppleLanguageProperties(
      (obj \ "language").as[String],
      (obj \ "display").as[String],
      (obj \ "description").as[String],
      (obj \ "publicationName").as[String]
    )
  }

  implicit def jsonToApplePricingProperties(obj: JsValue): ApplePricingProperties = {
    new ApplePricingProperties(
      (obj \ "clearedForSale").as[Boolean],
      (obj \ "price").as[Double],
      new PricingAvailability(
        (obj \ "pricingAvailability" \ "begin").as[Date],
        (obj \ "pricingAvailability" \ "end").as[Date]
      )
    )
  }

  implicit def jsonToAppleDurationProperties(obj: JsValue): AppleDurationProperties = {
    new AppleDurationProperties(
      (obj \ "autoRenewalDuration").as[Date],
      (obj \ "freeTrialDuration").as[Date],
      (obj \ "marketingIncentiveDuration").as[Date]
    )
  }
}

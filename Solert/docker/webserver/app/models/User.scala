package models

import org.joda.time.DateTime
import play.api.data._
import play.api.data.Forms.{list, longNumber, mapping, nonEmptyText, optional, text}
import play.api.data.validation.Constraints.pattern
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

case class User(
                    id: Option[String],
                    username: String,
                    password: String,
                    email: Option[String],
                    locations: List[String],
                    creationDate: Option[DateTime],
                    updateDate: Option[DateTime])

// Turn off your mind, relax, and float downstream
// It is not dying...
object User {
  import play.api.libs.json._

  implicit object UserWrites extends OWrites[User] {
    def writes(user: User): JsObject = Json.obj(
      "_id" -> user.id,
      "username" -> user.username,
      "password" -> user.password,
      "email" -> user.email,
      "locations" -> user.locations,
      "creationDate" -> user.creationDate.fold(-1L)(_.getMillis),
      "updateDate" -> user.updateDate.fold(-1L)(_.getMillis))
  }

  implicit object UserReads extends Reads[User] {
    def reads(json: JsValue): JsResult[User] = json match {
      case obj: JsObject => try {
        val id = (obj \ "_id").asOpt[String]
        val username = (obj \ "username").as[String]
        val password = (obj \ "password").as[String]
        val email = (obj \ "email").asOpt[String]
        val locations = (obj \ "locations").as[List[String]]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        val updateDate = (obj \ "updateDate").asOpt[Long]

        JsSuccess(User(id, username, password, email, locations,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  val form = Form(
    mapping(
      "id" -> optional(text verifying pattern(
        """[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> optional(text),
      "locations" -> optional(list(text)),
      "creationDate" -> optional(longNumber),
      "updateDate" -> optional(longNumber)) {
      (id, username, password, email, locations, creationDate, updateDate) =>
        User(
          id,
          username,
          password,
          email,
          locations.getOrElse(List()),
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_)))
    } { user =>
      Some(
        (user.id,
          user.username,
          user.password,
          user.email,
          Option(user.locations),
          user.creationDate.map(_.getMillis),
          user.updateDate.map(_.getMillis)))
    })
}

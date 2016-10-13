package controllers

import javax.inject.Inject

import org.joda.time.DateTime

import scala.concurrent.{Await, Future, Promise, duration}
import duration.Duration
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Request, Session}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.api.gridfs.{GridFS, ReadFile}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import models.User
import User._
import reactivemongo.bson.BSONDateTime

class Users @Inject() (
                        val messagesApi: MessagesApi,
                        val reactiveMongoApi: ReactiveMongoApi,
                        implicit val materializer: akka.stream.Materializer)
  extends Controller with MongoController with ReactiveMongoComponents {

  import java.util.UUID

  // get the collection 'articles'
  def collection = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("users"))

  // list all articles and sort them
  val index = Action.async { implicit request =>

    // get a sort document (see getSort method for more information)
    val sort: JsObject = getSort(request).getOrElse(Json.obj())

    val activeSort = request.queryString.get("sort").
      flatMap(_.headOption).getOrElse("none")

    // the cursor of documents
    val found = collection.map(_.find(Json.obj()).sort(sort).cursor[User]())

    // build (asynchronously) a list containing all the articles
    found.flatMap(_.collect[List]()).map { users =>
      Ok(views.html.users(users, activeSort))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  def showCreationForm = Action { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.editUser(None, User.form))
  }

  def showLoginForm = Action { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.login(User.form))
  }

  def showEditForm(id: String) = Action.async { implicit request =>

    def futureUser = collection.flatMap(
      _.find(Json.obj("_id" -> id)).one[User])

    for {
      maybeUser <- futureUser
      result <- Promise.successful(maybeUser.map { user =>
        implicit val messages = messagesApi.preferred(request)

        Ok(views.html.editUser(Some(id), User.form.fill(user)))
      }).future
    } yield result.getOrElse(NotFound)
  }


  def create = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    User.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editUser(None, errors))),

      // if no error, then insert the user into the 'users' collection
      user => collection.flatMap(_.insert(user.copy(
        id = user.id.orElse(Some(UUID.randomUUID().toString)),
        creationDate = Some(new DateTime()),
        updateDate = Some(new DateTime()))
      )).map(_ => Redirect(routes.Users.index))
    )
  }

  def edit(id: String) = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)
    import reactivemongo.bson.BSONDateTime

    User.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editUser(Some(id), errors))),

      user => {
        // create a modifier document, ie a document that contains the update operations to run onto the documents matching the query
        val modifier = Json.obj(
          // this modifier will set the fields
          // 'updateDate', 'title', 'content', and 'publisher'
          "$set" -> Json.obj(
            "updateDate" -> BSONDateTime(new DateTime().getMillis),
            "username" -> user.username,
            "password" -> user.password,
            "email" -> user.email))

        // ok, let's do the update
        collection.flatMap(_.update(Json.obj("_id" -> id), modifier).
          map { _ => Redirect(routes.Users.index) })
      })
  }

  def delete(id: String) = Action.async {
    // let's collect all the attachments matching that match the user to delete
    (for {

      coll <- collection
      _ <- {
        // now, the last operation: remove the user
        coll.remove(Json.obj("_id" -> id))
      }
    } yield Ok).recover { case _ => InternalServerError }
  }

  def login = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    User.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.login(errors))),

      user => {
        def futureUser = collection.flatMap(
          _.find(Json.obj(
            "username" -> user.username,
            "password" -> user.password)
          ).one[User])

        for {
          maybeUser <- futureUser
          result <- Promise.successful(maybeUser.map { user =>
            Redirect(routes.HomeController.index).withSession(new Session(Map("username"->user.username)))
          }).future
        } yield result.getOrElse(Redirect(routes.Users.login))
      })
  }

  def logout = Action(
    Redirect(routes.HomeController.index()).withNewSession
  )

  private def getSort(request: Request[_]): Option[JsObject] =
    request.queryString.get("sort").map { fields =>
      val sortBy = for {
        order <- fields.map { field =>
          if (field.startsWith("-"))
            field.drop(1) -> -1
          else field -> 1
        }
        if order._1 == "username" || order._1 == "email" || order._1 == "creationDate" || order._1 == "updateDate"
      } yield order._1 -> implicitly[Json.JsValueWrapper](Json.toJson(order._2))

      Json.obj(sortBy: _*)
    }

}

package controllers

import javax.inject.Inject

import org.joda.time.DateTime

import scala.concurrent.{ Await, Future, duration }, duration.Duration

import play.api.Logger

import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, Controller, Request }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ Json, JsObject, JsString }

import reactivemongo.api.gridfs.{ GridFS, ReadFile }

import play.modules.reactivemongo.{
MongoController, ReactiveMongoApi, ReactiveMongoComponents
}

import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import models.Article, Article._

class Articles @Inject() (
                           val messagesApi: MessagesApi,
                           val reactiveMongoApi: ReactiveMongoApi,
                           implicit val materializer: akka.stream.Materializer)
  extends Controller with MongoController with ReactiveMongoComponents {

  import java.util.UUID

  // get the collection 'articles'
  def collection = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("articles"))

  // list all articles and sort them
  val index = Action.async { implicit request =>
    // get a sort document (see getSort method for more information)
    val sort: JsObject = getSort(request).getOrElse(Json.obj())

    val activeSort = request.queryString.get("sort").
      flatMap(_.headOption).getOrElse("none")

    // the cursor of documents
    val found = collection.map(_.find(Json.obj()).sort(sort).cursor[Article]())

    // build (asynchronously) a list containing all the articles
    found.flatMap(_.collect[List]()).map { articles =>
      Ok(views.html.articles(articles, activeSort))
    }.recover {
      case e =>
        e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  def showCreationForm = Action { request =>
    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.editArticle(None, Article.form, None))
  }


  def create = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    Article.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editArticle(None, errors, None))),

      // if no error, then insert the article into the 'articles' collection
      article => collection.flatMap(_.insert(article.copy(
        id = article.id.orElse(Some(UUID.randomUUID().toString)),
        creationDate = Some(new DateTime()),
        updateDate = Some(new DateTime()))
      )).map(_ => Redirect(routes.Articles.index))
    )
  }

  def edit(id: String) = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)
    import reactivemongo.bson.BSONDateTime

    Article.form.bindFromRequest.fold(
      errors => Future.successful(
        Ok(views.html.editArticle(Some(id), errors, None))),

      article => {
        // create a modifier document, ie a document that contains the update operations to run onto the documents matching the query
        val modifier = Json.obj(
          // this modifier will set the fields
          // 'updateDate', 'title', 'content', and 'publisher'
          "$set" -> Json.obj(
            "updateDate" -> BSONDateTime(new DateTime().getMillis),
            "title" -> article.title,
            "content" -> article.content,
            "publisher" -> article.publisher))

        // ok, let's do the update
        collection.flatMap(_.update(Json.obj("_id" -> id), modifier).
          map { _ => Redirect(routes.Articles.index) })
      })
  }

  def delete(id: String) = Action.async {
    // let's collect all the attachments matching that match the article to delete
    (for {

      coll <- collection
      _ <- {
        // now, the last operation: remove the article
        coll.remove(Json.obj("_id" -> id))
      }
    } yield Ok).recover { case _ => InternalServerError }
  }

  private def getSort(request: Request[_]): Option[JsObject] =
    request.queryString.get("sort").map { fields =>
      val sortBy = for {
        order <- fields.map { field =>
          if (field.startsWith("-"))
            field.drop(1) -> -1
          else field -> 1
        }
        if order._1 == "title" || order._1 == "publisher" || order._1 == "creationDate" || order._1 == "updateDate"
      } yield order._1 -> implicitly[Json.JsValueWrapper](Json.toJson(order._2))

      Json.obj(sortBy: _*)
    }

}

package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import service.UserService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Promise


@Singleton
class Report @Inject()(val userService: UserService) extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.report("Solert", List()))
  }

  def info(location: String) = Action.async { implicit request =>
    def futureUser = userService.findUserByName(request.session.get("username").getOrElse(""))

    val locations : List[String] = List()
    for {
      maybeUser <- futureUser
      result <- Promise.successful(maybeUser.map { foundUser => {
        locations.++(foundUser.locations)
      }
      }).future
    } yield Ok(views.html.report("Solert", locations))

  }
}

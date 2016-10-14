package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import service.UserService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Promise


@Singleton
class Report @Inject()(val userService: UserService) extends Controller {

  def index = info("Groningen")

  def info(location: String) = Action.async { implicit request =>
    def username = request.session.get("username").getOrElse("")
    def futureUser = userService.findUserByName(username)

    for {
      maybeUser <- futureUser
      result <- Promise.successful(maybeUser.map { foundUser => {
        Ok(views.html.report("Solert " + location, foundUser.locations))
      }
      }).future
    } yield result.getOrElse(
      Ok(views.html.report("Solert " + location, List()))
    )
  }
}

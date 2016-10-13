package controllers

import javax.inject._
import play.api._
import play.api.mvc._


@Singleton
class Report @Inject() extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Solert", List()))
  }

  def info(location: String) = Action { implicit request =>
    Ok(views.html.index("Solert" + location, List()))
  }
}

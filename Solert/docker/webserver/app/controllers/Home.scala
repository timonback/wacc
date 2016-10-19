package controllers

import javax.inject._
import play.api._
import play.api.mvc._


@Singleton
class Home @Inject() extends Controller {

  def index = Action { implicit request =>
    Redirect(routes.Report.index)
  }
}

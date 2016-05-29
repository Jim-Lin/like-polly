package controllers

import java.io.File
import javax.inject._

import play.api.mvc._

@Singleton
class PictureController @Inject() extends Controller {

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      val filename = s"${java.util.UUID.randomUUID.toString.take(8)}_${picture.filename}"
//      val contentType = picture.contentType
      picture.ref.moveTo(new File(s"/tmp/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.HomeController.index()).flashing(
        "error" -> "Missing file")
    }
  }

}

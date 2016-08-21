package controllers

import java.io.File
import javax.inject._

import play.api.mvc._
import services.{TCroppedDetection, TFaceRecognition}

@Singleton
class PictureController @Inject() (croppedDetection: TCroppedDetection, faceRecognition: TFaceRecognition) extends Controller {

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      val uuid = java.util.UUID.randomUUID.toString.take(8)
      val srcFilename = s"${uuid}_${picture.filename}"
      val destFilename = s"${uuid}_cropped_${picture.filename}"
//      val contentType = picture.contentType
      def srcFile = new File(s"/tmp/$srcFilename")
      def destFile = new File(s"/tmp/$destFilename")
      picture.ref.moveTo(srcFile)
      croppedDetection.run(srcFile.getAbsolutePath, destFile.getAbsolutePath)
      faceRecognition.run(destFile.getAbsolutePath)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.HomeController.index()).flashing(
        "error" -> "Missing file")
    }
  }

}

package services

import java.io.File
import javax.inject.Singleton

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_objdetect._

import scala.io.Source

trait TCropped {
  def run(filePath: String)
}

@Singleton
class Cropped extends TCropped {

  val storage: CvMemStorage = cvCreateMemStorage()
  val xmlFile = getClass.getResource("/haarcascade_frontalface_default.xml")

  override def run(filePath: String) = {
    val file = getClass.getResource("/p.jpg")

    val orig: IplImage = cvLoadImage(file.getPath)

    // Creating rectangle by which bounds image will be cropped
    val r: CvRect = detectFace(orig)

    // After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
    cvSetImageROI(orig, r)
    val cropped: IplImage = cvCreateImage(cvGetSize(orig), orig.depth(), orig.nChannels())

    // Copy original image (only ROI) to the cropped image
    cvCopy(orig, cropped)
    def f = new File(s"/tmp/cropped.png")
    cvSaveImage(f.getPath, cropped)
  }

  private def detectFace(img: IplImage): CvRect = {
    cvClearMemStorage(storage)
    def cascade: CvHaarClassifierCascade = new CvHaarClassifierCascade(cvLoad(xmlFile.getPath))
    val cvSeq: CvSeq = cvHaarDetectObjects(img, cascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING, null, null)
    biggestRect(cvSeq)
  }

  private def biggestRect(faces: CvSeq): CvRect = {
    if (faces.sizeof() == 0) {
      return null
    }

    if (faces.sizeof() == 1) {
      return new CvRect(cvGetSeqElem(faces, 0))
    }

    var biggestIndex: Int = 0
    for (i <- 1 until faces.total()) {
      def biggest: CvRect = new CvRect(cvGetSeqElem(faces, biggestIndex))
      def current: CvRect = new CvRect(cvGetSeqElem(faces, i))
      if (current.width() * current.height() > biggest.width() * biggest.height()) {
        biggestIndex = i
      }
    }

    new CvRect(cvGetSeqElem(faces, biggestIndex))
  }
}

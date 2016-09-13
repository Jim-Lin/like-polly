package services

import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}
import java.io.File
import javax.imageio.ImageIO
import javax.inject.{Inject, Singleton}

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_objdetect._
import org.bytedeco.javacv.{Java2DFrameConverter, OpenCVFrameConverter}
import play.api.Configuration

import scala.collection.mutable.ArrayBuffer

trait TCroppedDetection {
  def run(srcPath: String, destPath: String)
}

@Singleton
class CroppedDetection @Inject()(config: Configuration) extends TCroppedDetection {

  final val XML_FILE = getClass.getResource(config.getString("haarcascade_frontalface_xml").get)

  val storage: CvMemStorage = cvCreateMemStorage()
  def converter: Java2DFrameConverter = new Java2DFrameConverter()

  override def run(srcPath: String, destPath: String) = {
    val source: IplImage = cvLoadImage(srcPath)

    // Creating rectangle by which bounds image will be cropped
    val face: CvRect = detectFace(srcPath)

    // After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
    cvSetImageROI(source, face)
    val cropped: IplImage = cvCreateImage(cvGetSize(source), source.depth(), source.nChannels())

    // Copy original image (only ROI) to the cropped image
    cvCopy(source, cropped)
    cvSaveImage(new File(destPath).getAbsolutePath, cropped)
    cvReleaseImage(cropped)
  }

  private def rotate(path: String, angle: Int): BufferedImage = {
    var buffer: BufferedImage = ImageIO.read(new File(path))
    def tx: AffineTransform = new AffineTransform()
    tx.rotate(angle, buffer.getWidth() / 2, buffer.getHeight() / 2)

    val op: AffineTransformOp = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR)
    buffer = op.filter(buffer, null)
    buffer
  }

  private def detectFace(srcPath: String): CvRect = {
    var rects = new ArrayBuffer[CvRect]
    def cascade: CvHaarClassifierCascade = new CvHaarClassifierCascade(cvLoad(XML_FILE.getPath))
    for (i <- -15 to 15) {
      val converter1: Java2DFrameConverter = new Java2DFrameConverter()
      val converter2: OpenCVFrameConverter.ToIplImage = new OpenCVFrameConverter.ToIplImage()
      val rotated: IplImage = converter2.convert(converter1.convert(rotate(srcPath, 3 * i)))

      cvClearMemStorage(storage)
      val cvSeq: CvSeq = cvHaarDetectObjects(rotated, cascade, storage)
      val rect: CvRect = getBiggestRect(cvSeq)
      if (rect.address() != 0) {
        rects += rect
      }
    }

    if (rects.isEmpty) {
      null
    } else {
      var biggestIndex: Int = 0
      for (i <- 1 until rects.length) {
        def biggest: CvRect = rects(biggestIndex)
        def current: CvRect = rects(i)
        if (current.width() * current.height() > biggest.width() * biggest.height()) {
          biggestIndex = i
        }
      }

      rects(biggestIndex)
    }

//    cvClearMemStorage(storage)
//    def cascade: CvHaarClassifierCascade = new CvHaarClassifierCascade(cvLoad(XML_FILE.getPath))
//    val cvSeq: CvSeq = cvHaarDetectObjects(img, cascade, storage)
//    getBiggestRect(cvSeq)
  }

  private def getBiggestRect(faces: CvSeq): CvRect = {
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

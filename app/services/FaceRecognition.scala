package services

import java.io.{File, FilenameFilter}
import java.nio.IntBuffer
import javassist.Loader
import javax.inject.Singleton

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_face._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_objdetect

trait TFaceRecognition {
  def run(filePath: String)
}

@Singleton
class FaceRecognition extends TFaceRecognition {

  final val TRAINING_DIR = "/Users/jim/Downloads/train"

  override def run(filePath: String) = {
    val testImage: Mat = imread(s"/Users/jim/Downloads/13938575_10207245531317302_1184454981818028210_n.jpg", CV_LOAD_IMAGE_GRAYSCALE)

    def root: File = new File(TRAINING_DIR)

    def imgFilter: FilenameFilter = new FilenameFilter() {
      def accept(dir: File, name: String): Boolean = {
        val lowerCaseName: String = name.toLowerCase
        lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".pgm") || lowerCaseName.endsWith(".png")
      }
    }

    val imageFiles: Array[File] = root.listFiles(imgFilter)

    def images: MatVector = new MatVector(imageFiles.length)
    def labels: Mat = new Mat(imageFiles.length, 1, CV_32SC1)
    def labelsBuf: IntBuffer = labels.createBuffer()
//    var labels: Array[String] = new Array[String](imageFiles.length)
    var counter = 0
    imageFiles.foreach { image: File =>
      val img: Mat = imread(image.getAbsolutePath, CV_LOAD_IMAGE_GRAYSCALE)
      val label: Int = Integer.parseInt(image.getName.split("_")(0))
      images.put(counter, img)
      labelsBuf.put(counter, label)
      counter += 1
    }

    val faceRecognizer: FaceRecognizer = createLBPHFaceRecognizer()
    faceRecognizer.train(images, labels)

    val predictedLabel = faceRecognizer.predict(testImage)
    println(s"Hello, world!   $predictedLabel")
  }

  private def detectFace(img: IplImage): CvRect = {
    null
  }
}

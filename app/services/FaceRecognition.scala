package services

import java.io.{File, FilenameFilter}
import java.nio.IntBuffer
import javax.inject.{Inject, Singleton}

import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_face._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_objdetect
import play.api.Configuration

trait TFaceRecognition {
  def run(filePath: String)
}

@Singleton
class FaceRecognition @Inject()(config: Configuration) extends TFaceRecognition {

  // https://github.com/bytedeco/javacv/issues/470
  Loader.load(classOf[opencv_objdetect])

  final val TRAINING_DATA_PATH: String = config.getString("training_data_path").get

  override def run(filePath: String) = {
    val testImage: Mat = imread(filePath, CV_LOAD_IMAGE_GRAYSCALE)
    val (images: MatVector, labels: Mat) = findImagesAndLabels()

    val faceRecognizer: FaceRecognizer = createLBPHFaceRecognizer()
    faceRecognizer.train(images, labels)

    val label = faceRecognizer.predict(testImage)
    println(s"Hello, world!   $label")
  }

  private def findImagesAndLabels(): (MatVector, Mat) = {
    def root: File = new File(TRAINING_DATA_PATH)

    def imgFilter: FilenameFilter = new FilenameFilter() {
      def accept(dir: File, name: String): Boolean = {
        val lowerCaseName: String = name.toLowerCase
        lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".pgm") || lowerCaseName.endsWith(".png")
      }
    }

    val imageFiles: Array[File] = root.listFiles(imgFilter)

    val images: MatVector = new MatVector(imageFiles.length)
    val labels: Mat = new Mat(imageFiles.length, 1, CV_32SC1)
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

    (images, labels)
  }
}

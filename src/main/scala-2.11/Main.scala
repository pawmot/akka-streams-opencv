import java.io.ByteArrayInputStream

import org.opencv.core.{Size, Core, Mat, MatOfByte}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture

import scala.concurrent.Future
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color._

object Main extends JFXApp {
  import scala.concurrent.ExecutionContext.Implicits._

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  val imgProp = ObjectProperty(new Image("http://nil"))
  def setImg(img: Image): Unit = {
      imgProp.set(img)
    }
  def getImg = imgProp.get

  var cap: Option[VideoCapture] = None

  stage = new PrimaryStage {
    title = "Camera test"
    scene = new Scene(800, 600) {
      fill = BLACK
      content = new HBox {
        padding = Insets(20)
        content = Seq(
          new ImageView {
            image <== imgProp
          }
        )
      }
    }
  }

  Future({
    cap = Some(new VideoCapture(0))
    val locCap = cap.get
    locCap.grab()

    while(true) {
      val mat = new Mat()
      while(!locCap.read(mat)) {
        Thread.sleep(1) // spin-wait
      }

//      val bwMat = new Mat()
//      Imgproc.cvtColor(mat, bwMat, Imgproc.COLOR_RGB2GRAY)

//      val blurredMat = new Mat()
//      Imgproc.blur(mat, blurredMat, new Size(15, 15))

      val matOfByte = new MatOfByte()
      Imgcodecs.imencode(".bmp", mat, matOfByte)

      val img = new Image(new ByteArrayInputStream(matOfByte.toArray))

      Platform.runLater(setImg(img))
    }
  })

  override def stopApp(): Unit = {
    cap.foreach(c => c.release())

    super.stopApp()
  }
}

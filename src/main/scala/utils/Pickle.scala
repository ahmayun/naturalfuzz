package utils
import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.io.{File, PrintWriter}
import org.json4s._
import org.json4s.jackson.Serialization

object Pickle {
  def dump(obj: Any, file: String): Unit = {
    val outputStream = new ObjectOutputStream(new FileOutputStream(file))
    outputStream.writeObject(obj)
    outputStream.close()
  }

  def load[T](file: String): T = {
    val inputStream = new ObjectInputStream(new FileInputStream(file))
    val obj = inputStream.readObject.asInstanceOf[T]
    inputStream.close()
    obj
  }

  implicit val formats = DefaultFormats

  def serialize(obj: AnyRef, fileName: String): Unit = {
    val file = new File(fileName)
    val pw = new PrintWriter(file)
    try {
      pw.write(Serialization.write(obj))
    } finally {
      pw.close()
    }
  }

  def deserialize[T](str: String)(implicit mf: Manifest[T]): T = {
    Serialization.read[T](str)
  }

}
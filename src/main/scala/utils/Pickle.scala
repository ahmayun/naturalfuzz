package utils
import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

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
}
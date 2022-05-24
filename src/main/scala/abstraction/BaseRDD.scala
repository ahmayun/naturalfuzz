package abstraction

import scala.reflect.ClassTag

class BaseRDD[T:ClassTag](val data: Seq[T]) extends RDD[T]{

  def filter(f:T=>Boolean): BaseRDD[T] = {
    new BaseRDD(data.filter(f))
  }

  def map[U:ClassTag](f: T => U): BaseRDD[U] = {
    new BaseRDD(data.map(f))
  }

  def collect(): Array[T] = {
    data.toArray
  }

  override def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U] = new BaseRDD(data.flatMap(f))

  override def take(num: Int): Array[T] = ???

  override def setName(name: String): BaseRDD.this.type = ???

  override def sortBy[K](f: T => K, ascending: Boolean)
                        (implicit ord: Ordering[K], ctag: ClassTag[K]): RDD[T] =
    new BaseRDD(data.sortBy(f))
}


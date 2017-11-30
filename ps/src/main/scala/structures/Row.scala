package structures

import scala.reflect.ClassTag

/**
  * Created by chris on 11/14/17.
  */
class Row[T:ClassTag](implicit ev: String => T){
  var rowId:Int = _
  var values:Array[T] = _
  var columns:Array[Int] = _
  var len:Int = _

  def this(str:String)(implicit ev: String => T) {
    this()
    parse(str)(ev)
  }

  def parse(str:String)(ev:String => T) :Unit = {
    rowId = Integer.parseInt(str.split(" ")(0))
    val features = str.split(" ").tail
    len = features.length
    values = Array.ofDim[T](len)
    columns = Array.ofDim[Int](len)
    (0 until len) foreach{i =>
      val ind = Integer.parseInt(features(i).split(":")(0))
      val value = ev.apply(features(i).split(":")(1))
      columns(i) = ind
      values(i) = value
    }
  }
}

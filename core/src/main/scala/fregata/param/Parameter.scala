package fregata.param

import fregata._
import fregata.util.VectorUtil

/**
 * Created by takun on 16/9/19.
 */
object ParameterServer {
  def create : ParameterServer = new LocalParameterServer
}

trait ParameterServer extends Serializable {
  def init(rows:Int,cols:Int)
  def adjust(delta:Array[Num],x:Vector)
  def get : Array[Vector]
  def set(ps:Array[Vector])
}

class LocalParameterServer extends ParameterServer {

  private[this] var ps : Array[DenseVector] = _
  private[this] var values : Array[Array[Num]] = _

  def init(rows:Int,cols:Int) = {
    values = Array.fill(rows)( Array.ofDim[Num](cols) )
    ps = values.map( new DenseVector(_) )
  }
  def set(ps:Array[Vector]) = {
    this.ps = ps.map( _.toDenseVector )
    values = this.ps.map(_.data)
  }
  def adjust(delta:Array[Num],x:Vector) = {
    var k = 0
    while( k < delta.length ) {
      val d = delta(k)
      VectorUtil.forV(x,(i,xi) =>{
          values(k)(i) -= d * xi
      })
      values(k)(values(k).length - 1) -= d
      k += 1
    }
  }
  def get : Array[Vector] = ps.asInstanceOf[Array[Vector]]
}
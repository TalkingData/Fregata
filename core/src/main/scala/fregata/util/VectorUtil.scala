package fregata.util

import fregata._

/**
 * Created by takun on 16/9/19.
 */
object VectorUtil {

  def wxpb(w:Vector,x:Vector,b:Double) : Double = {
    w match {
      case dv : DenseVector => wxpb(dv,x,b)
      case sv : SparseVector => wxpb(sv,x,b)
    }
  }

  def wxpb(w:SparseVector,x:Vector,b:Double) : Double = {
    val wb = w(w.size-1)
    var sum = wb * b
    forV(x, (i,xi) => {
      sum += w(i) * xi
    })
    sum
  }
  def wxpb(w:DenseVector,x:Vector,b:Double) : Double = {
    val wb = w(w.size-1)
    var sum = wb * b
    forV(x, (i,xi) => {
      sum += w(i) * xi
    })
    sum
  }

  def sameAs(nums:Array[Num],x:Vector) = x match {
    case sv : SparseVector =>
      new SparseVector(sv.index,nums,sv.length)
    case dv : DenseVector => new DenseVector(nums)
  }

  def forV(x:Vector , f : (Int,Num) => Unit ) = x match {
    case sv : SparseVector =>
      var j = 0
      while( j < sv.activeSize ) {
        f( sv.indexAt(j) , sv.valueAt(j))
        j += 1
      }
    case dv : DenseVector => dv.foreachPair( f )
  }

}

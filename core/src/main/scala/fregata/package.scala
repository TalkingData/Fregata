
import breeze.linalg.{Vector => BVector , SparseVector => BSparseVector , DenseVector => BDenseVector}

/**
 * Created by takun on 16/9/19.
 */
package object fregata {
  type Num = Double
  type Vector = BVector[Num]
  type SparseVector = BSparseVector[Num]
  type DenseVector = BDenseVector[Num]
  def zeros(n:Int) = BDenseVector.zeros[Num](n)
  def norm(x:Vector) = breeze.linalg.norm(x,2.0)
  def asNum(v:Double) : Num = v
//  def asNum(v:Double) : Num = v.toFloat
}

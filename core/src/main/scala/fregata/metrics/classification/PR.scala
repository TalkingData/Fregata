package fregata.metrics.classification

import fregata.Num
/**
 * Created by takun on 16/6/1.
 */
object PR {

  def compute(p:Double,y:Double) =
  // (tp,tn,fn,fp)
    if( y == 1 && p == 1 ) (1d,0d,0d,0d)
    else if( y == 0 && p == 0 ) (0d,1d,0d,0d)
    else if( y == 1 && p == 0 ) (0d,0d,1d,0d)
    else if( y == 0 && p == 1 ) (0d,0d,0d,1d)
    else (0d,0d,0d,0d)

  private def pr(it:Iterable[(Num,Num)]) = it.map{
    case (p,y) =>
      compute(p,y)
  }.reduce{
    (a,b) =>
      (a._1+b._1,
        a._2+b._2,
        a._3+b._3,
        a._4+b._4)
  }

  def f1(it:Iterable[(Num,Num)]) = fß(it,1)

  def fß(it:Iterable[(Num,Num)],ß:Num) = {
    val ß2 = ß * ß
    val (p,r) = of(it)
    (1+ß2) * p * r / ( ß2 * p + r)
  }

  /**
   *
   * @param it
   * @return (precision,recall)
   */
  def of(it:Iterable[(Num,Num)]) = {
    val (tp,tn,fn,fp) = pr(it)
    (tp / (tp+fp) , tp / (tp+fn) )
  }

}

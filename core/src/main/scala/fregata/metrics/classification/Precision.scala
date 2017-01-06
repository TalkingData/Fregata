package fregata.metrics.classification

import fregata.Num

/**
 * Created by takun on 16/6/1.
 */
object Precision {

  def compute(p:Double,y:Double) = if( y == p ) 1 else 0

  def of(it:Iterable[(Num,Num)]) = {
    var sum = 0d
    var size = 0
    it.foreach{
        case (p,y) =>
          if (y == 1) {
            sum += compute(p, y)
            size += 1
          }
    }
    sum / size
  }
}

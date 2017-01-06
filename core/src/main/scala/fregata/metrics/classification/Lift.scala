package fregata.metrics.classification

import fregata.Num

/**
 * Created by takun on 16/6/1.
 */
object Lift {

  def compute(p:Double,y:Double) = if( y == p ) 1 else 0

  def of(it:Iterable[(Num,Num)], amp:Double) = {
    var sum = 0d
    var n = 0 // num of instance
    var m = 0d // num of positive instance

    it.foreach{
      case (p,y) =>
        if (y == 1) {
          m += 1
        }
        n += 1
    }

    val it_sort = it.toMap.toList.sortWith(_._1 > _._1).slice(0, (amp*m).toInt)
    it_sort.foreach{
        case (p,y) =>
          if (y == 1) {
            sum += 1
          }
    }

    sum*n / (m*m*amp)
  }
}

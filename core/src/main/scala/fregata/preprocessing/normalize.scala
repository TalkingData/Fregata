package fregata.preprocessing

import breeze.linalg.minMax
import fregata._

/**
 * Created by takun on 16/8/9.
 */
object normalize {
  def apply(x:Vector) = {
    val (_min,_max) = minMax(x)
    val r = (x - _min ) / (_max - _min)
    r.asInstanceOf[Vector]
  }
}
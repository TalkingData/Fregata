package fregata.optimize

import fregata._

/**
 * Created by takun on 16/9/19.
 */
trait Gradient extends Serializable{
  def calculate(x:Vector,label:Num) : Array[Num]
}

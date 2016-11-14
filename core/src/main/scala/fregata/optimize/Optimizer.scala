package fregata.optimize

import fregata._

/**
 * Created by takun on 16/9/19.
 */
trait Optimizer extends Serializable {
  def run(data:TraversableOnce[(Vector,Num)])
}

package fregata.optimize.sgd

import fregata._

/**
 * Created by takun on 16/9/29.
 */
class AdaptiveSGD extends StochasticGradientDescent {

  override def stepSize(i:Int,x:Vector) = asNum(1d)

}

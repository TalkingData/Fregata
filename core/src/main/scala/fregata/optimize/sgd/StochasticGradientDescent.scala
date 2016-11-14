package fregata.optimize.sgd

import fregata._
import fregata.optimize.Minimizer

/**
 * Created by takun on 16/9/19.
 */
class StochasticGradientDescent extends Minimizer {

  private var eta = asNum(.1)
  def setStepSize(eta:Num) : this.type = {
    this.eta = eta
    this
  }
  protected def stepSize(itN:Int,x:Vector) = eta

  def run(data:TraversableOnce[(Vector,Num)]) = {
    var i = 0
    data.foreach{
      case (x,label) =>
        val gradients = target.gradient.calculate(x,label)
        val step = stepSize(i,x)
        val delta = gradients.map( v => asNum( v * step ) )
        target.ps.adjust(delta,x)
        i += 1
    }
  }
}

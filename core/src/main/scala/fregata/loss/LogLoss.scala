package fregata.loss

import fregata._

/**
 * Created by takun on 16/6/1.
 */
class LogLoss extends LossFunction {

  private val signifi = 1e-15
  private val max_loss = math.log(signifi)

  def of(data:Iterable[(Num,Num,Num)]) = {
    var size = 0
    var sum = 0.0
    data.map{
      case (label,pred_label,prob) =>
        sum += calculate(label,pred_label,prob)
        size += 1
    }
    asNum(sum / size)
  }

  def calculate(label:Num, pred_label:Num, prob:Num) = {
    if( prob < signifi ) {
      asNum( - max_loss )
    }else{
      asNum( - math.log(prob) )
    }
  }

  /**
   * for best performance , here not multiply x
   * @see fregata.param.ParameterServer
   * @param x
   * @param prob
   * @param label
   * @return
   */
  def gradient(x:Vector,prob:Num,label:Num) : Num = {
    val g = prob - label
    asNum(g)
  }
}

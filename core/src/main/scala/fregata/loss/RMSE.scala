package fregata.loss

import fregata._

/**
 * Created by takun on 16/6/1.
 */
class RMSE extends LossFunction {

  def of(data:Iterable[(Num,Num,Num)]) = {
    var size = 0
    var sum = 0.0
    data.map{
      case (label,pred_label,prob) =>
        sum += calculate(label,pred_label,prob)
        size += 1
    }
    asNum(math.sqrt(sum / size))
  }

  def calculate(label:Num, pred_label:Num, prob:Num) = {
    asNum(math.pow(pred_label - label,2.0))
  }

  /**
   * for best performance , here not multiply x
   * @see fregata.param.ParameterServer
   * @param x
   * @param predict
   * @param label
   * @return
   */
  def gradient(x:Vector,predict:Num,label:Num) : Num = {
    val g = predict - label
    asNum(g)
  }
}

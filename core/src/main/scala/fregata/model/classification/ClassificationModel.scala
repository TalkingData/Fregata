package fregata.model.classification

import fregata._
import fregata.model.Model

/**
 * Created by takun on 16/9/20.
 */
trait ClassificationModel extends Model {

  def predict(x:Vector) : Num = classPredict(x)._2

  /**
    * @param x input vector
    * @return (predict probability , predict class)
    */
  def classPredict(x:Vector) : (Num,Num)
  def classPredict(data:S[(Vector,Num)]) : S[((Vector,Num),(Num,Num))] = data.map{
    case a @ (x,label) =>
      val (p,c) = classPredict(x)
      (a,(p,c))
  }

  def saveModel(fn: String): Int = 0
  def loadMode(fn: String): Int = 0
}

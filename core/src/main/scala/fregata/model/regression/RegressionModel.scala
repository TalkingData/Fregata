package fregata.model.regression

import fregata._
import fregata.model.Model

/**
  * Created by takun on 2016/10/12.
  */
trait RegressionModel extends Model {
  def regressionPredict(data:S[(Vector,Num)]) = data.map{
    case a @ (x,label) =>
      val value = predict(x)
      (a,value)
  }
}
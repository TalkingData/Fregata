package fregata.metrics

import fregata._

/**
  * Created by takun on 2016/10/12.
  */
package object classification {
  def accuracy(data:Iterable[(Num,Num)]) = Accuracy.of(data)
  def auc(data:Iterable[(Num,Num)]) = AreaUnderRoc.of(data)
}
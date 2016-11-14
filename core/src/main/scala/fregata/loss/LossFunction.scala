package fregata.loss

import fregata._

/**
 * Created by takun on 16/9/19.
 */
trait LossFunction extends Serializable{
  def of(data:Iterable[(Num,Num,Num)]) : Num
  def calculate(label:Num,pred_label:Num,prob:Num) : Num
  def gradient(x:Vector,predict:Num,label:Num) : Num
}

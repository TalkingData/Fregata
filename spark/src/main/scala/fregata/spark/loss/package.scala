package fregata.spark

import fregata._
import fregata.loss.{LogLoss, LossFunction, RMSE}
import org.apache.spark.rdd.RDD

/**
  * Created by takun on 2016/10/12.
  */
package object loss {

  private def calculate(data:RDD[(Num,Num,Num)],lossF:LossFunction) = {
    data.mapPartitions{
      it =>
        var size = 0
        var sum = 0.0
        it.foreach{
          case (label,pred_label,prob) =>
            sum += lossF.calculate(label,pred_label,prob)
            size += 1
        }
        Iterator( (sum,size) )
    }.treeReduce{
      (a,b) => (a._1+b._1,a._2+b._2)
    }
  }
  def log(data:RDD[(Num,Num,Num)]) = {
    val (sum,size) = calculate(data,new LogLoss)
    sum / size
  }
  def rmse(data:RDD[(Num,Num,Num)]) = {
    val (sum,size) = calculate(data,new RMSE)
    math.sqrt(sum / size)
  }
}

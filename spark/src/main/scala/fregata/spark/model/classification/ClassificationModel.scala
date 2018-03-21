package fregata.spark.model.classification

import fregata._
import fregata.model.classification.{ClassificationModel => LClassificationModel}
import fregata.spark.model.SparkModel
import org.apache.spark.rdd.RDD

/**
  * Created by takun on 2016/10/12.
  */
trait ClassificationModel extends SparkModel{
  def model : LClassificationModel

  /**
    * predict class
    * @param x
    * @return
    */
  def classPredict(x:Vector) = model.predict(x)
  /**
    * batch predict class
    * @param data
    * @return
    */
  def classPredict(data:S[(Vector,Num)]) = model.classPredict(data)
  /**
    * batch predict class for RDD
    * @param data
    * @return
    */
  def classPredict(data:RDD[(Vector,Num)]) = {
    predictPartition[(Vector,Num),(Num,Num)](data,{
      case ((x,label),model:LClassificationModel) => model.classPredict(x)
    })
  }

  def saveModel(fn: String): Int = {
    model.saveModel(fn)
  }
}

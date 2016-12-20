package fregata.spark.model.regression

import fregata._
import fregata.model.classification.SoftMaxModel
import fregata.model.regression.{RegressionModel => LRegressionModel}
import fregata.spark.model.SparkModel
import org.apache.spark.rdd.RDD

/**
  * Created by takun on 2016/10/12.
  */
trait RegressionModel extends SparkModel{
  def model : LRegressionModel
  def regressionPredict(data:RDD[(Vector,Num)]) = {
    predictPartition[(Vector,Num),(Num)](data,{
      case ((x,label),model:LRegressionModel) => model.predict(x)
    })
  }
}

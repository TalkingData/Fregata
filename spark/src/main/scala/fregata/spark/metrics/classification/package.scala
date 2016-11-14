package fregata.spark.metrics

import fregata._
import org.apache.spark.rdd.RDD

/**
  * Created by takun on 2016/10/12.
  */
package object classification {
  def auc(rdd:RDD[(Num,Num)]) = AreaUnderRoc.of(rdd)
  def accuracy(rdd:RDD[(Num,Num)]) = Accuracy.of(rdd)
}

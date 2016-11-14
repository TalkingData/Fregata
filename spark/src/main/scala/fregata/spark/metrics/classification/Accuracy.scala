package fregata.spark.metrics.classification

import fregata.metrics.classification.Accuracy._
import org.apache.spark.rdd.RDD

import fregata.Num

/**
 * Created by takun on 16/6/1.
 */
object Accuracy {

  def of(rdd:RDD[(Num,Num)]) = {
    val (sum,size) = rdd.map{
      case (p,y) =>
        val v = compute(p,y)
        (v,1)
    }.treeReduce{
      case ((v1,s1),(v2,s2)) =>
        (v1 + v2 , s1 + s2)
    }
    sum.toDouble / size.toDouble
  }
}

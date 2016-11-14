package fregata.spark.metrics.classification

import fregata.metrics.classification.PR._
import org.apache.spark.rdd.RDD

/**
 * Created by takun on 16/6/1.
 */
object PR {

  def confuseMatrix(rdd:RDD[(Double,Double)]) = {
    val counts = rdd.map( _ -> 1).reduceByKey( _ + _ ).collectAsMap
    val max = counts.flatMap{ case ((c1,c2),_) => Array(c1,c2)}.max.toInt + 1
    val matrix = Array.fill[Array[Int]](max)(Array.ofDim[Int](max))
    counts.foreach{ case ((c1,c2),n) => matrix(c1.toInt)(c2.toInt) = n }
    matrix
  }

  private def pr(rdd:RDD[(Double,Double)]) = rdd.map{
      case (p,y) => compute(p,y)
    }.treeReduce{
      (a,b) =>
        (a._1+b._1,
          a._2+b._2,
          a._3+b._3,
          a._4+b._4)
    }

  def f1(rdd:RDD[(Double,Double)]) = fß(rdd,1)
  def fß(rdd:RDD[(Double,Double)],ß:Double) = {
    val ß2 = ß * ß
    val (p, r) = of(rdd)
    (1 + ß2) * p * r / (ß2 * p + r)
  }

  /**
   *
   * @param rdd
   * @return (precision,recall)
   */
  def of(rdd:RDD[(Double,Double)]) = {
    val (tp,_,fn,fp) = pr(rdd)
    (tp / (tp+fp) , tp / (tp+fn) )
  }
}

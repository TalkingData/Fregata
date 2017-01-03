package fregata.spark.metrics.classification

import org.apache.spark.rdd.RDD
import fregata.Num

import scala.util.Random

/**
 * Created by takun on 16/6/1.
 */
object AreaUnderRoc {

  /**
   * RDD< ( score , class ) >
   * @param rs2
   * @return
   */
  def of(rs2:RDD[(Num, Num)]) = {
    val rs = rs2.map{
      case (score,clazz) =>
        (score + Random.nextFloat()*0.00001f) -> clazz
    }.sortByKey(false)
    val total = rs2.count()
    val (m,sum) = rs.zipWithIndex().map{
      case ((predict,label),rank) =>
        if( label == 1 ) {
          predict -> ( total - rank , 1L , 1 , 0)
        }else{
          predict -> ( total - rank , 1L , 0 , 1)
        }
    }.reduceByKey{
      case ((r1,c1,p1,f1),(r2,c2,p2,f2)) =>
        (r1+r2 ,c1+c2,p1+p2,f1+f2)
    }.map{
      case (score,(rank,count,positive,navigate)) =>
        val avg = rank.toDouble / count
        (positive,avg * positive)
    }.filter( _._1 > 0 ).treeReduce{
      case ((p1,r1),(p2,r2)) => (p1+p2,r1+r2)
    }
    val M = m.toDouble
    if( M == 0 || M == total ) 0.5
    else{
      val N = total - M
      val diff = sum - ( M * ( M + 1 ) / 2 )
      diff / (M * N)
    }
  }
}

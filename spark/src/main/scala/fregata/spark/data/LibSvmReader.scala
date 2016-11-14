package fregata.spark.data

import fregata._
import fregata.data.LibSvmReader._
import org.apache.spark.SparkContext

/**
 * Created by takun on 16/8/4.
 */
object LibSvmReader {

  def read(sc:SparkContext,path:String,numFeatures : Int = -1 , minPartition : Int = -1 ) = {
    val mp = if( minPartition <= 0 ) sc.defaultMinPartitions else minPartition
    val parsed = sc.textFile(path,mp).flatMap{
      l => mapLine(l)
    }
    val n = if( numFeatures <= 0 ) {
      parsed.map { case (label, indices, values) =>
        indices.lastOption.getOrElse(0)
      }.max + 1
    } else numFeatures
    (n,parsed.map{
      case (label,indices,values) =>
        (new SparseVector(indices,values,n).asInstanceOf[Vector] , asNum(label) )
    })
  }
}

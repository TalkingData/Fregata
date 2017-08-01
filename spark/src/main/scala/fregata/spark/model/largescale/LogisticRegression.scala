package fregata.spark.model.largescale

import fregata._
import org.apache.spark.rdd.RDD

import scala.collection.mutable

class LogisticRegression {
  val t = 0.95
  var step = 0.0
  var i = 0.0
  val weight : mutable.Map[Long,Double] = new mutable.HashMap[Long,Double]()
  def calculate(indices:Array[Long],values:Array[Num],label:Num) = {
    val lambda = i / ( i + 1 )
    i += 1
    var margin = 0d
    indices.indices.foreach{
      i =>
        margin += weight.getOrElse(indices(i),0.0) * values(i)
    }
    val p1 = 1.0 / ( 1.0 + math.exp( - margin ) )
    val p0 = 1 - p1
    val b1 = math.exp(p1)
    val b0 = math.exp(p0)
    val x2 = values.map(math.pow(_,2)).sum
    val y = if( label == 1 ) {
      (p1 - t) / ( t * (1 - p0 * b0 - p1 * b1) + p1 * (1 - b0) ) / x2
    }else{
      (p0 - t) / ( t * (1 - p0 * b0 - p1 * b1 ) + p0 * (1 - b1)) / x2
    }
    step = lambda * step + (1 - lambda) * y
    val delta = 2 * ( p1 - label ) * step
    indices.indices.foreach{
      i =>
        val w = weight.getOrElse(indices(i),0.0) - delta * values(i)
        weight(indices(i)) = w
    }
  }

  def run(data:Iterator[(Array[Long],Array[Num],Num)],epoch:Int) = {
    (0 until epoch).foreach{
      i => data.foreach{
        case (indices,values,label) => calculate(indices,values,label)
      }
    }
  }
}

case class LogisticRegressionModel(weights:CompressedArray) {
  def predict(indices:Array[Long],values:Array[Num]) : Double = {
    var margin = 0d
    indices.indices.foreach{
      i =>
        margin += weights(indices(i)) * values(i)
    }
    1.0 / (1.0 + math.exp(-margin))
  }

  def predict(data:RDD[(Array[Long],Array[Num],Num)], threshold:Double = 0.5 ):RDD[((Array[Long],Array[Num],Num),(Num,Num))] = {
    val br_array = data.sparkContext.broadcast(this)
    data.map{
      case input @ (indices,values,label) =>
        val p = br_array.value.predict(indices,values)
        val c = if( p < threshold ) asNum(0) else asNum(1)
        input -> (asNum(p),asNum(c))
    }
  }
}

object LogisticRegression {

  def run(data:RDD[(Array[Long],Array[Num],Num)], binSize : Int = 128, epoch : Int = 1, feature_threshold:Double = 1e-4 ) = {
    val weights = data.mapPartitionsWithIndex{
      case (idx,it) =>
        val local = new LogisticRegression()
        local.run(it,epoch)
        local.weight.iterator
    }.filter( _._2.abs > feature_threshold ).map{
      case (idx,w) => idx -> (w,1)
    }.reduceByKey{
      case ((w1,c1),(w2,c2)) => (w1+w2) -> (c1+c2)
    }.map{
      case (idx,(w,c)) => idx -> (w / c)
    }.filter( _._2.abs > feature_threshold )
    LogisticRegressionModel(CompressedArray.compress(weights,binSize))
  }
}
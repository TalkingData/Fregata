package fregata.spark.model.largescale

import fregata._
import fregata.loss.LogLoss
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
  * Created by takun on 2017/3/24.
  */
class SoftMax(k:Int) {
  val loss = new LogLoss
  var i = 0.0
  val thres = .95
  var stepSize = 0d
  val weights = Array.fill(k)(new mutable.HashMap[Long,Double]())

  def calculate(indices:Array[Long],values:Array[Num],label:Num) = {
    val lambda = i / (i+1)
    i += 1
    // compute greedy step size
    val (_ps,_) = predict(indices,values)
    val yi = label.toInt
    val x2 = values.map( math.pow(_,2) ).sum
    val pi = _ps(yi)
    // compute averaged step size
    val greedyStep = ( pi - thres ) / (thres * ( 1.0 - _ps.map( p => p * math.exp(p) ).sum ) + pi - math.E * pi / math.exp(pi) ) / x2
    stepSize = lambda * stepSize + (1-lambda) * greedyStep
    // compute update
    weights.indices.foreach { k =>
      val y = if( k == label ) asNum(1) else asNum(0)
      val gs = (_ps(k) - y) * stepSize
      indices.indices.foreach{
        i =>
          weights(k)(indices(i)) = weights(k).getOrElse(indices(i),0.0) - gs * values(i)
      }
    }
  }

  def predict(indices:Array[Long],values:Array[Num]) = {
    var maxI = 0
    var max = Double.NegativeInfinity
    var i = 0
    val margins = weights.map{
      w =>
        var m = 1.0
        indices.indices.foreach {
          i =>
            m += w.getOrElse(indices(i),0.0) * values(i)
        }
        m
    }
    val ps = margins.map {
      margin =>
        val sum = margins.map(m => math.exp( m - margin ) ).sum
        val p = 1.0 / sum
        if( p > max ) {
          max = p
          maxI = i
        }
        i += 1
        p
    }
    ( ps , maxI )
  }

  def run(data:Iterator[(Array[Long],Array[Num],Num)],epoch:Int) = {
    (0 until epoch).foreach{
      i => data.foreach{
        case (indices,values,label) => calculate(indices,values,label)
      }
    }
  }
}


case class SoftMaxModel(weights:Array[CompressedArray]) {
  def predict(indices:Array[Long],values:Array[Num]) = {
    var maxI = 0
    var max = Double.NegativeInfinity
    var i = 0
    val margins = weights.map{
      weight =>
        var m = 1.0
        indices.indices.foreach {
          i =>
            m += weight(indices(i)) * values(i)
        }
        m
    }
    val ps = margins.map {
      margin =>
        val sum = margins.map(m => math.exp( m - margin ) ).sum
        val p = 1.0 / sum
        if( p > max ) {
          max = p
          maxI = i
        }
        i += 1
        asNum(p)
    }
    ( ps , maxI )
  }

  def predict(data:RDD[(Array[Long],Array[Num],Num)], threshold:Double = 0.5 ):RDD[((Array[Long],Array[Num],Num),(Array[Num],Num))] = {
    val br_array = data.sparkContext.broadcast(this)
    data.map{
      case input @ (indices,values,label) =>
        val (ps,clazz) = br_array.value.predict(indices,values)
        input -> (ps,asNum(clazz))
    }
  }
}

object SoftMax {
  def run(k:Int,data:RDD[(Array[Long],Array[Num],Num)], binSize : Int = 128, epoch : Int = 1, feature_threshold:Double = 1e-4 ) = {
    val weights = data.mapPartitionsWithIndex{
      case (idx,it) =>
        val local = new SoftMax(k)
        local.run(it,epoch)
        local.weights.zipWithIndex.iterator.flatMap{
          case (w,i) => w.map{
            case (key,value) => (key,i) -> value
          }
        }
    }.filter( _._2.abs > feature_threshold ).map{
      case (idx,w) => idx -> (w,1)
    }.reduceByKey{
      case ((w1,c1),(w2,c2)) => (w1+w2) -> (c1+c2)
    }.map{
      case (idx,(w,c)) => idx -> (w / c)
    }.filter( _._2.abs > feature_threshold )
    SoftMaxModel(CompressedArray.multiCompress(weights,binSize))
  }
}
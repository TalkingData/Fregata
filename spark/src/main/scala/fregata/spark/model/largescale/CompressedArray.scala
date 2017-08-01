package fregata.spark.model.largescale

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{DenseVector, Vector}
import org.apache.spark.rdd.RDD
import scala.collection.mutable

/**
  * Created by takun on 2016/12/13.
  */

class CompressedArray(val bits:Int) extends Serializable{
  var data:Array[Double] = _
  val indices = Array.fill(bits)(new Int64BitMap)

  def put(fromIndex:Long,toIndex:Int) : this.type = {
    var b = 0
    val bi = toIndex + 1
    while( b < bits ) {
      if( ((bi >> b) & 0x1) == 1 ) {
        indices(b).add( fromIndex )
      }
      b += 1
    }
    this
  }

  def get(fromIndex:Long) = {
    var index = 0
    (0 until bits).foreach{
      bi =>
        if( indices(bi).contains(fromIndex) ) {
          index |= 1 << bi
        }
    }
    index - 1
  }

  def apply(index:Long) = {
    val i = get(index)
    if( i < 0 ) 0d else data(i)
  }

  def add(other:CompressedArray) : this.type = {
    assert(other.bits == bits , "bits must be equal .")
    (0 until bits).foreach{
      bi =>
        indices(bi).or(other.indices(bi))
    }
    this
  }

  def setData(data:Array[Double]): this.type = {
    this.data = data
    this
  }

  def getData = data
}

object CompressedArray {
  def compress(weights:RDD[(Long, Double)],bin_size : Int = 128) = {
    multiCompress(weights.map{
      case (idx,value) => (idx,0) -> value
    },bin_size)(0)
  }

  def multiCompress(weights:RDD[((Long,Int), Double)],bin_size : Int = 128) = {
    val data = weights.map{
      case (idx,value) => idx -> new DenseVector(Array(value)).asInstanceOf[Vector]
    }
    val model = KMeans.train(data.map( _._2 ),bin_size,10)
    val br_model = weights.sparkContext.broadcast(model)
    val bin_bit = math.ceil(math.log(bin_size + 1) / math.log(2)).toInt
    val arrays = data.mapPartitions{
      it =>
        val model = br_model.value
        val arrays = mutable.Map[Int,CompressedArray]()
        it.foreach{
          case ((idx,ki),value) =>
            val c = model.predict(value)
            val compressedArray = arrays.get(ki) match {
              case None =>
                val array = new CompressedArray(bin_bit)
                arrays.put(ki,array)
                array
              case Some(_array) => _array
            }
            compressedArray.put(idx,c)
        }
        Iterator(arrays)
    }.treeReduce{
      (a,b) =>
        b.foreach{
          case (ki,_array) => a.get(ki) match {
            case Some(array2) => array2.add(_array)
            case None => a.put(ki,_array)
          }
        }
        a
    }
    val compressValues = model.clusterCenters.map( _(0) )
    val k = arrays.keys.max
    Array.tabulate(k+1) {
      i =>
        val array = arrays.get(i) match {
          case None => new CompressedArray(bin_bit)
          case Some(_array) => _array
        }
        array.setData(compressValues)
    }
  }
}
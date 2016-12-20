package fregata.spark.model.classification

import fregata._
import fregata.model.classification.{RDTModel => LRDTModel, RDTClassification=>LRDTClassification}
import scala.collection.mutable.{HashMap => MHashMap, ArrayBuffer}
import org.apache.spark.rdd.RDD

/**
  * Created by hjliu on 16/10/31.
  */

class RDTModel(val model: LRDTModel) extends ClassificationModel {
  def rdtPredict(data:RDD[(Vector,Num)]) = {
    predictPartition[(Vector,Num),(Array[Num],Int)](data,{
      case ((x,label),model:LRDTModel) => model.rdtPredict(x)
    })
  }
}

class RDTClassification(numTrees: Int, numFeatures: Int, numClasses: Int, depth: Int,
                        minLeafCapacity: Int, maxPruneNum: Int, seed:Long = 20170315l) extends Serializable {

  def train(datas: RDD[(Vector, Num)]) = {
    val (trees, models, seeds) = datas.mapPartitions { it =>
      val rdt = new LRDTClassification(numTrees, depth, numFeatures, numClasses, seed)
      rdt.train(it.toArray)
      rdt.prune(minLeafCapacity, maxPruneNum)
      Array[(Array[MHashMap[Long, Int]], MHashMap[(Int, (Long, Byte)), Array[Int]], ArrayBuffer[Int])](
        (rdt.getTrees, rdt.getModels, rdt.getSeeds)
      ).iterator
    }.treeReduce { (m1, m2) =>

      var i = 0
      while (i < m1._1.length) {
        m1._1.apply(i) ++= m2._1.apply(i)
        i += 1
      }

      for ((k, count) <- m2._2) {
        m1._2.getOrElse(k, Array.ofDim[Int](numClasses)) match {
          case count_ =>
            (0 until numClasses).foreach(i=>count(i)+=count_(i))
            m1._2.update(k, count)
        }
      }
      m1
    }

    new RDTModel(new LRDTModel(depth, numClasses, seeds.toArray, trees, models))
  }
}
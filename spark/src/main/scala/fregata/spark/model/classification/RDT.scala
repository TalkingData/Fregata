package fregata.spark.model.classification

import fregata._
import fregata.model.classification.{RDT => LRDT, RDTModel => LRDTModel}
import scala.collection.mutable.{HashMap => MHashMap, ArrayBuffer}
import org.apache.spark.rdd.RDD

/**
  * Created by hjliu on 16/10/31.
  */

class RDTModel(val model: LRDTModel) extends ClassificationModel {

  /**
    * predict to get every class probability
    * @param data input vector
    * @return
    */
  def rdtPredict(data:RDD[(Vector,Num)]) = {
    predictPartition[(Vector,Num),(Array[Num],Int)](data,{
      case ((x,label),model:LRDTModel) => model.rdtPredict(x)
    })
  }
}

/**
  * the RandomDecisionTree algorithm
  * @param numTrees the number of trees
  * @param depth the depth of each tree
  * @param numFeatures the number of features of each instance
  * @param numClasses the number of classes of the input instance
  * @param minLeafCapacity the minimum number of instances in leaf
  * @param maxPruneNum the maximum prune layer of one path
  * @param seed used to generate seeds(each RDT's seed)
  */
class RDT(numTrees: Int, numFeatures: Int, numClasses: Int, depth: Int,
          minLeafCapacity: Int, maxPruneNum: Int, seed:Long = 20170315l) extends Serializable {

  /**
    * train the RDT model
    * @param datas input training datas
    * @return RDTModel used to predict
    */
  def train(datas: RDD[(Vector, Num)]) = {
    val (trees, models, seeds) = datas.mapPartitions { it =>
      val rdt = new LRDT(numTrees, depth, numFeatures, numClasses, seed)
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

      for ((k, negPos) <- m2._2) {
        m1._2.getOrElse(k, Array(0, 0)) match {
          case negPos_ =>
            m1._2.update(k, negPos_.zip(negPos).map(t => t._1 + t._2))
        }
      }
      m1
    }

    new RDTModel(new LRDTModel(depth, numClasses, seeds.toArray, trees, models))
  }
}
package fregata.spark.model.regression

import fregata._
import fregata.model.regression.{RDTRegressionModel => LRDTRegressionModel, RDTRegression => LRDTRegression}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.{ArrayBuffer, HashMap=>MHashMap}

/**
  * Created by hjliu on 16/11/28.
  */

class RDTRegressionModel(val model:LRDTRegressionModel) extends RegressionModel

class RDTRegression(numTrees: Int, numFeatures: Int, depth: Int, minLeafCapacity: Int,
          maxPruneNum: Int, seed:Long = 20170315l) extends Serializable {

  def train(datas: RDD[(Vector, Num)]) = {
    val (trees, models, seeds) = datas.mapPartitions { it =>
      val rdtRegression = new LRDTRegression(numTrees, depth, numFeatures, seed)
      rdtRegression.train(it.toArray)
      rdtRegression.prune(minLeafCapacity, maxPruneNum)
      Array[(Array[MHashMap[Long, Int]], MHashMap[(Int, (Long, Byte)), (Num, Int)], ArrayBuffer[Int])](
        (rdtRegression.getTrees, rdtRegression.getModels, rdtRegression.getSeeds)
      ).iterator
    }.treeReduce { (m1, m2) =>
      var i = 0

      while (i < m1._1.length) {
        m1._1.apply(i) ++= m2._1.apply(i)
        i += 1
      }

      for ((k, yn) <- m2._2) {
        m1._2.getOrElse(k, (0d, 0)) match {
          case yn_ =>
            m1._2.update(k, (yn._1+yn_._1, yn._2+yn_._2))
        }
      }
      m1
    }

    new RDTRegressionModel(new LRDTRegressionModel(depth, seeds.toArray, trees, models))
  }
}

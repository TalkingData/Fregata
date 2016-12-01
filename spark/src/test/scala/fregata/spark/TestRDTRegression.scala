package fregata.spark

import fregata.spark.data.LibSvmReader
import fregata.spark.model.regression.RDTRegression
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by hjliu on 16/11/28.
  */
object TestRDTRegression {

  def main(args: Array[String]) {
    val inTrain = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/dna.scale"
    val inPredict = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/dna.scale.t"
    val numTrees = 32
    val numFeatures = 180
    val depth = 32
    val minLeafCapacity = 10
    val maxPruneNum = 5

//    val conf = new SparkConf().setAppName("rdt_regression")
//    val sc = new SparkContext(conf)
    val sc = new SparkContext("local", "rdt_regression")
    val (_, trainData) = LibSvmReader.read(sc, inTrain, numFeatures.toInt)
    val (_, testData) = LibSvmReader.read(sc, inPredict, numFeatures.toInt)
    val rdt = new RDTRegression(numTrees, numFeatures, depth, minLeafCapacity, maxPruneNum)
    val model = rdt.train(trainData)

    val sum = model.regressionPredict(testData).mapPartitions{ it =>
      var sum  = 0d
      var size = 0
      it.foreach{
        case ((x,l),p) =>
          sum += math.pow( p - l , 2 )
          size += 1
      }
      Array((sum, size)).iterator
    }.collect().reduce((t1, t2)=>(t1._1+t2._1, t1._2+t2._2))
    val rmse = math.sqrt(sum._1 / sum._2)
    println(s"rmse=$rmse")
  }

}

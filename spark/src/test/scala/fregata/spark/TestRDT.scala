package fregata.spark

import fregata._
import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.{AreaUnderRoc, Accuracy}
import org.apache.spark.{SparkConf, SparkContext}
import fregata.spark.model.classification.RDT

/**
  * Created by hjliu on 16/10/31.
  */
object TestRDT {

  def main(args: Array[String]) {
    val inTrain = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a"
    val inPredict = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a.t"
    val numTrees = 100
    val numFeatures = 123
    val numClasses = 2
    val depth = 32
    val minLeafCapacity = 10
    val maxPruneNum = 5

    val conf = new SparkConf().setAppName("rdt")
    val sc = new SparkContext(conf)
    val (_, trainData) = LibSvmReader.read(sc, inTrain, numFeatures.toInt)
    val (_, testData) = LibSvmReader.read(sc, inPredict, numFeatures.toInt)
    val rdt = new RDT(numTrees, numFeatures, numClasses, depth, minLeafCapacity, maxPruneNum)
    val model = rdt.train(trainData)

    val predicted = model.rdtPredict(testData)
    val auc = AreaUnderRoc.of(predicted.map {
      case ((x, l), (p, c)) =>
        p(1) -> l
    })

    val loss = fregata.spark.loss.log(predicted.map {
      case ((x, l), (ps, c)) =>
        (l, asNum(c), ps(l.toInt))
    })

    println(s"AreaUnderCurve = $auc ")
    println(s"logLoss : $loss ")
  }

}
package fregata.test

import fregata._
import fregata.data.LibSvmReader
import fregata.loss.LogLoss
import fregata.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.model.classification.RDTClassification

/**
  * Created by hjliu on 16/10/27.
  */
object TestRDTClassification {

  def main(args: Array[String]) {
    val numFeatures = 123
    val (_, trainData) = LibSvmReader.read("/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a", numFeatures)
    val (_, testData) = LibSvmReader.read("/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a.t", numFeatures)
    println("load over ...")
    val numTrees = 100
    val depth = 32
    val rdt = new RDTClassification(numTrees, depth, numFeatures, numClasses = 2)

    val trainstart = System.currentTimeMillis()
    rdt.train(trainData)
    val trainend = System.currentTimeMillis()
    println(s"train=${trainend - trainstart}")
    val model = rdt.prune(10, 5)
    val pruneend = System.currentTimeMillis()
    println(s"prune=${pruneend - trainend}")
    val predicted = model.rdtPredict(testData)
    println(s"predict=${System.currentTimeMillis - pruneend}")

    val acc = Accuracy.of(predicted.map {
      case ((x, l), (probs, c)) =>
        asNum(c) -> l
    })

    val loss = new LogLoss().of(predicted.map {
      case ((x, l), (probs, c)) =>
        (l, asNum(c), probs(l.toInt))
    })

    val auc = AreaUnderRoc.of(predicted.map {
      case ((x, l), (p, c)) =>
        p(1) -> l
    })

    println(s"acc:$acc")
    println(s"logLoss:$loss")
    println(s"auc:$auc")
  }

}
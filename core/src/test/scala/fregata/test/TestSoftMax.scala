package fregata.test

import scala.util.Random
import fregata._
import fregata.preprocessing._
import fregata.model.classification.{LogisticRegressionModel, SoftMax, SoftMaxModel}
import fregata.data.LibSvmReader
import fregata.loss.LogLoss
import fregata.metrics.classification.Accuracy

/**
 * Created by takun on 16/9/19.
 */
object TestSoftMax {

  def main(args: Array[String]) {
    val (n1,t1) = LibSvmReader.read("/Volumes/takun/data/libsvm/mnist2",780)
    val (n2,t2) = LibSvmReader.read("/Volumes/takun/data/libsvm/mnist2.t",780)
    val trainData = t1.map{
      case (x,label) => normalize(x) -> label
    }
    val testData = t2.map{
      case (x,label) => normalize(x) -> label
    }
    val k = 10
    println(" load over ...")
    val data2 = Random.shuffle(trainData.toList)
    val model = new SoftMax(k)
      .run(data2,10 )
    val predicted = model.softMaxPredict(testData)
    val acc = Accuracy.of(predicted.map{
      case ((x,l),(ps,c)) =>
        asNum(c) -> l
    })
    val loss = new LogLoss().of(predicted.map{
      case ((x,l),(ps,c)) =>
        (l,asNum(c),ps(l.toInt))
    })
    println( s"Accuracy : ${acc} " )
    println( s"logLoss : ${loss} " )
  }
}

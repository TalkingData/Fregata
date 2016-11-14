package fregata.test

import breeze.linalg._
import fregata.model.classification.{LogisticRegressionModel, SoftMax, SoftMaxModel}

import scala.util.Random
import fregata.model.classification.LogisticRegression
import fregata.data.LibSvmReader
import fregata.loss.LogLoss
import fregata.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.{Vector => _, _}

/**
 * Created by takun on 16/9/19.
 */
object TestLogisticRegression {

  def main(args: Array[String]) {
    val (_,trainData) = LibSvmReader.read("/Volumes/takun/data/libsvm/a9a",123)
    val (_,testData) = LibSvmReader.read("/Volumes/takun/data/libsvm/a9a.t",123)
    println(" load over ...")
    val r = new Random(1L)
    val data2 = r.shuffle(trainData.toList)
    val model = new LogisticRegression()
      .run(data2,10)
    val predicted = model.classPredict(testData)
    val acc = Accuracy.of(predicted.map{
      case ((x,l),(p,c)) =>
        c -> l
    })
    val auc = AreaUnderRoc.of(predicted.map{
      case ((x,l),(p,c)) =>
        p -> l
    } )
    val loss = new LogLoss().of(predicted.map{
      case ((x,l),(p,c)) =>
        if( l == 1d ) {
          (l,c,p)
        }else{
          ( l , c , 1-p )
        }
    })
    println( s"Accuracy : ${acc} " )
    println( s"AreaUnderRoc : ${auc} " )
    println( s"logLoss : ${loss} " )
  }
}

package fregata.test

import breeze.linalg._
import fregata.data.LibSvmReader
import fregata.loss.LogLoss
import fregata.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.model.classification.{CLR, CLRModel, LogisticRegression, LogisticRegressionModel}

import scala.util.Random

/**
 * Created by takun on 16/9/19.
 */
object TestCLR {

  def main(args: Array[String]) {
    val (_,t1) = LibSvmReader.read("/Volumes/takun/data/libsvm/a9a",123)
    val (_,t2) = LibSvmReader.read("/Volumes/takun/data/libsvm/a9a.t",123)
    val trainData = t1.map{
      case (x,label) => Array(x) -> label
    }
    val testData = t2.map{
      case (x,label) => Array(x) -> label
    }
    println(" load over ...")
    val r = new Random(1L)
    val data2 = r.shuffle(trainData.toList)
    val model = new CLR()
      .run(data2,Array(Array(0,0)) )
    val predicted = model.clrPredict(testData)
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

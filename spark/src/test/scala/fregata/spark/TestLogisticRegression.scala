package fregata.spark

import fregata._
import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.spark.model.classification.LogisticRegression
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by takun on 16/9/20.
 */
object TestLogisticRegression {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("logistic regression")
    val sc = new SparkContext(conf)
    val (_,trainData) = LibSvmReader.read(sc, args(0), Integer.parseInt(args(1)))
    val (_,testData) = LibSvmReader.read(sc, args(2), Integer.parseInt(args(1)))
    val model = LogisticRegression.run(trainData, lastModel = args(3))
    val pd = model.classPredict(testData)
    val acc = Accuracy.of( pd.map{
      case ((x,l),(p,c)) =>
        c -> l
    })
    val auc = AreaUnderRoc.of( pd.map{
      case ((x,l),(p,c)) =>
        p -> l
    })
    val loss = fregata.spark.loss.log(pd.map{
      case ((x,l),(p,c)) =>
        if( l == 1d ) {
          (l,c,p)
        }else{
          ( l , c , 1-p )
        }
    })
    println( s"AreaUnderRoc = $auc ")
    println( s"Accuracy = $acc ")
    println( s"LogLoss = $loss ")
    model.saveModel(args(3))
  }
}

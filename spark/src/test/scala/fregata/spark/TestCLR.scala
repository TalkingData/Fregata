package fregata.spark

import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.spark.model.classification.{CLR, LogisticRegression}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by takun on 16/9/20.
 */
object TestCLR {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("logistic regression")
    val sc = new SparkContext(conf)
    val (_,trainData) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/a9a",123)
    val (_,testData) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/a9a.t",123)
    val model = CLR.run(trainData.map{
      case (x,label) => Array(x) -> label
    },Array(Array(0,0)),10)
    val pd = model.clrPredict(testData.map{
      case (x,label) => Array(x) -> label
    })
    val acc = Accuracy.of( pd.map{
      case ((x,l),(p,c)) =>
        c -> l
    })
    println( s"Accuracy = $acc ")
    val auc = AreaUnderRoc.of( pd.map{
      case ((x,l),(p,c)) =>
        p -> l
    })
    println( s"AreaUnderRoc = $auc ")
  }
}

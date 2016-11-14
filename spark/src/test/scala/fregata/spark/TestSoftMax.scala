package fregata.spark

import fregata.spark.data.LibSvmReader
import fregata._
import fregata.spark.metrics.classification.Accuracy
import fregata.spark.model.classification.SoftMax
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by takun on 16/9/20.
 */
object TestSoftMax {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("soft max")
    val sc = new SparkContext(conf)
    val (_,t1) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/mnist2",780)
    val (_,t2) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/mnist2.t",780)
    val k = 10
    val train = t1.map{
      case (x,label) => x -> label
    }
    val testD = t2.map{
      case (x,label) => x -> label
    }
    val model = SoftMax.run(k.toInt,train)
    val pd = model.softMaxPredict(testD)
    val acc = Accuracy.of( pd.map{
      case ((x,l),(ps,c)) =>
        asNum(c) -> l
    })
    val loss = fregata.spark.loss.log(pd.map{
      case ((x,l),(ps,c)) =>
        (l,asNum(c),ps(l.toInt))
    })
    println( s"Accuracy = $acc ")
    println( s"LogLoss = $loss ")
  }
}

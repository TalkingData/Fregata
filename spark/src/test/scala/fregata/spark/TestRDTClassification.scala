package fregata.spark

import fregata._
import fregata.hash.SimHash
import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.Accuracy
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import fregata.spark.model.classification.RDTClassification

/**
  * Created by hjliu on 16/10/31.
  */
object TestRDTClassification {

  def main(args: Array[String]) {
    //    val Array(inTrain, inPredict, numTrees, numFeatures, depth,
    //    minLeafCapacity, maxPruneNum, threshold, out) = args

    val inTrain = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/mnist2"
    val inPredict = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/mnist2.t"
    val numTrees = 32
    var numFeatures = 780
    val numClasses = 10
    val depth = 32
    val minLeafCapacity = 10
    val maxPruneNum = 5
    val numSimhashbits = 512

    var trainRdd: RDD[(fregata.Vector, fregata.Num)] = null
    var testRdd : RDD[(fregata.Vector, fregata.Num)] = null

    val conf = new SparkConf().setAppName("rdt")
    val sc = new SparkContext(conf)
    val (_, trainData) = LibSvmReader.read(sc, inTrain, numFeatures)
    val (_, testData) = LibSvmReader.read(sc, inPredict, numFeatures)

    if(numSimhashbits>0){
      val bc_simhash = sc.broadcast(new SimHash(numFeatures, numSimhashbits))
      trainRdd = trainData.map(inst => bc_simhash.value.hash(inst._1)->inst._2)
      testRdd  = testData.map(inst => bc_simhash.value.hash(inst._1)->inst._2)
      numFeatures = numSimhashbits
    }else{
      trainRdd = trainData
      testRdd  = testData
    }

    val rdt = new RDTClassification(numTrees, numFeatures, numClasses, depth, minLeafCapacity, maxPruneNum)
    val model = rdt.train(trainData)
    val predicted = model.rdtPredict(testData)
    val acc = Accuracy.of(predicted.map {
      case ((x, l), (p, c)) =>
        l -> c
    })
    println(s"Acc = $acc ")

    val loss = fregata.spark.loss.log(predicted.map{
      case ((x,l),(ps,c)) =>
        (l,asNum(c),ps(l.toInt))
    })

    println( s"logLoss : $loss " )
  }
}

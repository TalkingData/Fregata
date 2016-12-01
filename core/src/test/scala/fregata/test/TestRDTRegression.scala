package fregata.test

import fregata.data.LibSvmReader
import fregata.model.regression.RDTRegression

/**
  * Created by hjliu on 16/11/28.
  */
object TestRDTRegression {

  def main(args: Array[String]) {
    val numFeatures = 123
    val (_, trainData) = LibSvmReader.read("/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a", numFeatures)
    val (_, testData) = LibSvmReader.read("/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a.t", numFeatures)
    println("load over ...")
    val numTrees = 32
    val depth = 32
    val rdt = new RDTRegression(numTrees, depth, numFeatures)

    rdt.train(trainData)
    val model = rdt.prune(10, 5)
    var sum  = 0d
    var size = 1
    model.regressionPredict(testData).foreach{
      case ((x,l),p) =>
        sum += math.pow( p - l , 2 )
        size += 1
    }
    val rmse = math.sqrt( sum / size )
    println( s"RMSE = " + rmse )
  }

}

import fregata.SparseVector
import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.{Accuracy, AreaUnderRoc}
import fregata.spark.model.largescale.LogisticRegression
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by takun on 16/9/20.
  */
object TestLargeScaleLogisticRegression {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("logistic regression")
    val sc = new SparkContext(conf)
    val (_,trainData) = LibSvmReader.read(sc, args(0), args(1).toInt)
    val (_,testData) = LibSvmReader.read(sc, args(2), args(1).toInt)
    val model = LogisticRegression.run(trainData.map{
      case (x,label) =>
        val sx = x.asInstanceOf[SparseVector]
        (sx.index.map( _.toLong ) , sx.data,label)
    },binSize = 32)
    val pd = model.predict(testData.map{
      case (x,label) =>
        val sx = x.asInstanceOf[SparseVector]
        (sx.index.map( _.toLong ) , sx.data,label)
    })
    val acc = Accuracy.of( pd.map{
      case ((x,v,l),(p,c)) =>
        c -> l
    })
    val auc = AreaUnderRoc.of( pd.map{
      case ((x,v,l),(p,c)) =>
        p -> l
    })
    val loss = fregata.spark.loss.log(pd.map{
      case ((x,v,l),(p,c)) =>
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


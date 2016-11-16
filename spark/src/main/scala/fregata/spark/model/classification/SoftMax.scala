package fregata.spark.model.classification

import fregata._
import fregata.model.classification.{SoftMax => LSoftMax, SoftMaxModel => LSoftMaxModel}
import fregata.spark.model.{ SparkTrainer}
import org.apache.spark.rdd.RDD

/**
  * SoftMax is multi-class classification algorithm , generalization of Logistic Regression .
  * Created by takun on 16/9/20.
 */
class SoftMaxModel(val model:LSoftMaxModel) extends ClassificationModel {
  /**
    * predict to get every class probability
    * @param data
    * @return
    */
  def softMaxPredict(data:RDD[(Vector,Num)]) = {
    predictPartition[(Vector,Num),(Array[Num],Int)](data,{
      case ((x,label),model:LSoftMaxModel) => model.softMaxPredict(x)
    })
  }
}

object SoftMax {

  /**
    *
    * @param k class number
    * @param data training data
    * @param localEpochNum the local model epoch num of every partition
    * @param epochNum
    * @return @see fregata.spark.model.classification.SoftMaxModel
    */
  def run(k:Int,
          data:RDD[(Vector,Num)],
          localEpochNum:Int = 1 ,
          epochNum:Int = 1) = {
    val trainer = new LSoftMax(k)
    new SparkTrainer(trainer)
      .run(data,epochNum,localEpochNum)
    new SoftMaxModel(trainer.buildModel(trainer.ps))
  }
}

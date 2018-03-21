package fregata.spark.model.classification

import fregata._
import fregata.spark.model.{ SparkTrainer}
import fregata.model.classification.{LogisticRegressionModel => LLogisticRegressionModel, LogisticRegression => LLogisticRegression}
import org.apache.spark.rdd.RDD

/**
  * Logistic Regression is used to estimate the probability of a binary response based on one or more predictor (or independent) variables (features) .
 * Created by takun on 16/9/20.
 */

class LogisticRegressionModel(val model:LLogisticRegressionModel) extends ClassificationModel

object LogisticRegression {

  /**
    *
    * @param data
    * @param localEpochNum the local model epoch num of every partition
    * @param epochNum
    * @return
    */
  def run(data:RDD[(Vector,Num)],
          localEpochNum:Int = 1 ,
          epochNum:Int = 1,
          lastModel: String = "") = {
    val trainer = new LLogisticRegression
    trainer.loadModel(lastModel)
    new SparkTrainer(trainer)
      .run(data,epochNum,localEpochNum)
    new LogisticRegressionModel(trainer.buildModel(trainer.ps))
  }
}

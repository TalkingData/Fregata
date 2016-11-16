package fregata.spark.model.classification

import fregata._
import fregata.spark.model.{ SparkTrainer}
import fregata.model.classification.{LogisticRegression, CLR => LCLR, CLRModel => LCLRModel}
import org.apache.spark.rdd.RDD

/**
 * Created by takun on 16/9/20.
 */
class CLRModel(val model:LCLRModel) extends ClassificationModel {
  def clrPredict(data:RDD[(Array[Vector],Num)]) = {
    predictPartition[(Array[Vector],Num),(Num,Num)](data,{
      case ((x,label),model:LCLRModel) => model.clrPredict(x)
    })
  }
}

/**
  * beta version
  * CLR is combine features logistic regression,
  * most time recommendation or click model need combine features
  */
object CLR {
  /**
    * train CLRModel
    * @param data input data , the row is the dimension features
    * @param combines the groups of combine features ,
    *                 for example , Array(Array(0,0),Array(0,1)) combine the first self and combine the first and second
    * @param localEpochNum the local model epoch num of every partition
    * @param epochNum
    * @return
    */
  def run(data:RDD[(Array[Vector],Num)],
          combines:Array[Array[Int]],
          localEpochNum:Int = 1 ,
          epochNum:Int = 1) = {
    val trainer = new LogisticRegression
    val lengths = combines.map{
      comb => comb.map(i=>data.first._1(i).length).reduce( _ * _ )
    }
    val length = lengths.sum
    val data2 = data.map{
      case (x,label) =>
        LCLR.compactVector(x,combines,lengths,length) -> label
    }
    new SparkTrainer(trainer)
      .run(data2,epochNum,localEpochNum)
    new CLRModel(new LCLRModel(trainer.ps.get(0),combines))
  }
}

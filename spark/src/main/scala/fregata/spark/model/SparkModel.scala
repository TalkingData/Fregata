package fregata.spark.model

import fregata._
import fregata.model.Model
import org.apache.spark.rdd.RDD

/**
 * Created by takun on 16/9/20.
 */
trait SparkModel extends Model {
  def model : Model
  def predict(x:Vector) = model.predict(x)
  private [spark] def predictPartition[T,R](data:RDD[T] , p : (T,Model) => R ) = {
    val m = this.model
    val br_model = data.sparkContext.broadcast(m)
    data.mapPartitions{
      case it =>
        val model = br_model.value
        it.map{
          x => (x , p(x,model))
        }
    }
  }

  /**
    * predict
    * @param data
    * @return
    */
  def predict(data:RDD[Vector]) = {
    predictPartition[Vector,Num](data,(x,model) => model.predict(x) )
  }

}
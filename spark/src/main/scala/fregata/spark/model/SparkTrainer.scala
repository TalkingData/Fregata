package fregata.spark.model

import fregata._
import fregata.model.{ ModelTrainer}
import org.apache.spark.rdd.RDD

/**
 * Created by takun on 16/9/20.
 */
class SparkTrainer(trainer:ModelTrainer) {

  def run(data:RDD[(Vector,Num)],epochNum:Int,localEpochNum:Int) {
    (0 until epochNum).foreach{
      i =>
        run(data,localEpochNum)
    }
  }

  def run(data:RDD[(Vector,Num)],localEpochNum:Int) {
    val _trainer = this.trainer
    val br_opt = data.sparkContext.broadcast(_trainer)
    val pn = data.partitions.length
    val ws = data.mapPartitions{
      it =>
        val local_opt = br_opt.value
        local_opt.run(it.toIterable,localEpochNum)
        Iterator( local_opt.ps.get )
    }.treeReduce{
      (a,b) =>
        a.zip(b).map{
          case (w1,w2) => w1 + w2
        }
    }
    ws.foreach{
      w =>
      val values = w match {
        case w : DenseVector => w.data
        case w : SparseVector => w.data
      }
      var i = 0
      while( i < values.length ) {
        values(i) /= pn
        i += 1
      }
    }
    trainer.ps.set(ws)
  }
}

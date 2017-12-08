package adni

import adni.AdniModel._
import com.tencent.angel.conf.AngelConf.ANGEL_PS_NUMBER
import com.tencent.angel.ml.conf.MLConf
import com.tencent.angel.ml.conf.MLConf.{DEFAULT_ML_PART_PER_SERVER, DEFAULT_ML_WORKER_THREAD_NUM, ML_PART_PER_SERVER, ML_WORKER_THREAD_NUM}
import com.tencent.angel.ml.feature.LabeledData
import com.tencent.angel.ml.math.vector.{DenseFloatVector, DenseIntVector}
import com.tencent.angel.ml.model.{MLModel, PSModel}
import com.tencent.angel.ml.predict.PredictResult
import com.tencent.angel.protobuf.generated.MLProtos.RowType
import com.tencent.angel.worker.storage.DataBlock
import com.tencent.angel.worker.task.TaskContext
import org.apache.hadoop.conf.Configuration

/**
  * Created by chris on 10/30/17.
  */
object AdniModel {
  // hyper parameter settings
  val c_1 = "ml.adni.c1"

  val c_2 =  "ml.adni.c2"

  val c_3 =  "ml.adni.c3"

  val c_4 =  "ml.adni.c4"

  val c_5 =  "ml.adni.c5"

  val c_6 =  "ml.adni.c6"

  val Phi = "ml.adni.phi"

  val K = "ml.adni.k"

  val B = "ml.adni.b"

  val U = "ml.adni.u"

  val S = "ml.adni.s"

  val nodes = "ml.adni.V"

  val Vol = "ml.adni.vol"

  val Feq = "ml.adni.update"

  // model setting
  val model = "membership"

  val indi = "indicator"

  val SAVE_MODEL = "save.model"


}

class AdniModel(conf: Configuration, _ctx: TaskContext = null) extends MLModel(conf, _ctx) {

  // hyperparameter and checking
  val c1:Float = conf.getFloat(c_1, 200f)
  val c2:Float = conf.getFloat(c_2, 280f)
  val c3:Float = conf.getFloat(c_3, 1800f)
  val c4:Float = conf.getFloat(c_4, 140f)
  val c5:Float = conf.getFloat(c_5, 20f)
  val c6:Float = conf.getFloat(c_6, 60f)
  val phi:Float = conf.getFloat(Phi, 0.8f)
  val V:Int = conf.getInt(nodes, 0)
  val vol:Long = conf.getLong(Vol, 0l)
  val feq:Int = conf.getInt(Feq, 1)
  val s:Int = conf.getInt(S,10)
  val k:Int = conf.getInt(K, 0)
  val b:Int = Math.min(conf.getInt(B, 0),61)
  val u:Int = conf.getInt(U, 0)
  this.check()
  val (l, tlast, epslion) = lTlastEpsilon()
  val epoch:Int = conf.getInt(MLConf.ML_EPOCH_NUM, tlast)


  val save:Boolean = conf.getBoolean(SAVE_MODEL, true)
  val threadNum: Int = conf.getInt(ML_WORKER_THREAD_NUM, DEFAULT_ML_WORKER_THREAD_NUM)
  val psNum: Int = conf.getInt(ANGEL_PS_NUMBER, 1)
  val parts: Int = conf.getInt(ML_PART_PER_SERVER, DEFAULT_ML_PART_PER_SERVER)

  val mVec:PSModel[DenseFloatVector] = PSModel[DenseFloatVector](model, 2, V, 2, V / psNum)
    .setRowType(RowType.T_FLOAT_DENSE)
    .setOplogType("DENSE_FLOAT")
  addPSModel(mVec)

  val indicator:PSModel[DenseIntVector] = PSModel[DenseIntVector](indi, 1, 1, 1, 1)
    .setRowType(RowType.T_INT_DENSE)
    .setOplogType("DENSE_INT")

  addPSModel(indicator)

  override
  def predict(dataSet: DataBlock[LabeledData]): DataBlock[PredictResult] = {
    null
  }

  def check():Unit = {
    if(k > u) throw new Exception("diffusion users should be less than users in total")
    if(c2 < 2*c4) throw new Exception("condition 2 violated")
    if(c6 < 2*c5) throw new Exception("condition 3 violated")
    if(c3 < 8*c5) throw new Exception("condition 4 violated")
    if(c4 < 4*c5) throw new Exception("condition 5 violated")
    if((0.5/c6 - 1.0/c3 - 0.5/c5/c6) < 1.0/c4) throw new Exception("condition 6 violated")
    if(0.5/c5 < (1.2/c6 + 1.0/c1)) throw new Exception("condition 7 violated")
    if(0.2 < (1.0/c5 + (4.0*c6) / (3.0*c3) + 0.5/c1 + 0.5/ c2)) throw new Exception("condition 8 violated")
  }

  def lTlastEpsilon(): (Int, Int, Float)= {
    val l = Math.ceil((Math.log(vol)/ Math.log(2d)) / 2)
    val tlast = (l + 1) * Math.ceil(2.0 /(phi * phi) * Math.log(c1 * (l +2) * Math.sqrt(vol / 2.0)))
    val epsilon = 1.0 / (c3 * (l + 2) * tlast * math.pow(2, b))
    (l.toInt, tlast.toInt, epsilon.toFloat)
  }
}

package adni

import com.tencent.angel.worker.storage.MemoryDataBlock
import com.tencent.angel.worker.task.{BaseTask, TaskContext}
import org.apache.commons.logging.LogFactory
import org.apache.hadoop.io.{LongWritable, Text}
import structures.{CSRMatrix, Row}

import scala.language.implicitConversions
/**
  * Created by chris on 11/14/17.
  */
object sf{
  implicit def string2float(str: String) = {str.toFloat}
}
class AdTrainTask(val ctx: TaskContext) extends BaseTask[LongWritable, Text, Row[Float]](ctx) {
  private val LOG = LogFactory.getLog(classOf[AdTrainTask])

  var incidence = new MemoryDataBlock[Row[Float]](-1)
  var N = 0
  var did = 0
  var rowId:Array[Int] = _

  override
  def parse(key: LongWritable, value: Text): Row[Float] = {
    import sf._
    val row  = new Row[Float](value.toString)
    if (row != null) {
      did += 1
      N += row.len
    }
    row
  }

  override
  def preProcess(ctx: TaskContext) {
    val reader = ctx.getReader[LongWritable, Text]
    while (reader.nextKeyValue()) {
      incidence.put(parse(reader.getCurrentKey(), reader.getCurrentValue))
    }
  }

  def build(shape:(Int,Int)):CSRMatrix[Float] = {
    rowId = Array.ofDim[Int](did)
    val values = Array.ofDim[Float](N)
    val columns = Array.ofDim[Int](N)
    val rows = Array.ofDim[Int](N)
    var count = 0
    (0 until did) foreach{i =>
      val row = incidence.get(i)
      rowId(i) = row.rowId
      (0 until row.len) foreach{j =>
        values(count) = row.values(j)
        columns(count) = row.columns(j)
        rows(count) = i
        count += 1
      }
    }
    incidence.clean()
    new CSRMatrix[Float](values,rows,columns,shape)
  }

  @throws[Exception]
  def run(ctx: TaskContext): Unit = {
    val model = new AdniModel(ctx.getConf, ctx)
    LOG.info(s"V=${model.V} K=${model.k} PartRows=$did Entries=$N" + s" threadNum=${model.threadNum}")
    val data = build((did, model.V))
    val seeds = rowId.filter{f =>
      f >= model.s
    }
    val learner = new AdLearner(ctx, model, data, rowId, seeds)
    learner.initialize()
    learner.train()
    if(model.save && ctx.getTaskIndex == 0) learner.saveResult()
  }
}

package adni

import com.tencent.angel.client.AngelClientFactory
import com.tencent.angel.conf.AngelConf
import com.tencent.angel.data.inputformat.BalanceInputFormat
import com.tencent.angel.ml.MLRunner
import org.apache.commons.logging.LogFactory
import org.apache.hadoop.conf.Configuration

/**
  * Created by chris on 11/15/17.
  */
class AdRunner extends MLRunner {
  val LOG = LogFactory.getLog(classOf[AdLearner])
  override def train(conf: Configuration): Unit = {
    conf.setInt(AngelConf.ANGEL_WORKER_MAX_ATTEMPTS, 1)
    conf.setInt(AngelConf.ANGEL_WORKER_TASK_NUMBER, 1)
    conf.set(AngelConf.ANGEL_INPUTFORMAT_CLASS, classOf[BalanceInputFormat].getName)
    LOG.info(s"n_tasks=${conf.getInt(AngelConf.ANGEL_WORKER_TASK_NUMBER, 0)}")

    val client = AngelClientFactory.get(conf)

    client.startPSServer()
    client.loadModel(new AdniModel(conf))
    client.runTask(classOf[AdTrainTask])
    client.waitForCompletion()
    client.stop()
  }
  override def predict(conf: Configuration): Unit = ???
  override def incTrain(conf: Configuration): Unit = ???

}

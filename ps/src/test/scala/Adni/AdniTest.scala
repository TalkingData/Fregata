package Adni

import adni.AdniModel._
import adni.{AdTrainTask, AdniModel}
import com.tencent.angel.client.{AngelClient, AngelClientFactory}
import com.tencent.angel.conf.AngelConf
import org.apache.commons.logging.{Log, LogFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat
import org.apache.log4j.PropertyConfigurator
import org.junit.{Before, Test}

/**
  * Created by chris on 11/28/17.
  */
class AdniTest {
  private val conf: Configuration = new Configuration
  private val LOG: Log = LogFactory.getLog(classOf[AdTrainTask])
  private val LOCAL_FS: String = FileSystem.DEFAULT_FS
  private val TMP_PATH: String = System.getProperty("java.io.tmpdir", "/tmp")
  private var client: AngelClient = null
  PropertyConfigurator.configure("./src/conf/log4j.properties")
  LOG.info(System.getProperty("user.dir"))
  @Before
  def setup(): Unit = {
    val inputPath: String = "./src/test/data/Adni/adni_data.csv"

    // Set basic configuration keys
    conf.setBoolean("mapred.mapper.new-api", true)
    conf.set(AngelConf.ANGEL_TASK_USER_TASKCLASS, classOf[AdTrainTask].getName)

    // Use local deploy mode
    conf.set(AngelConf.ANGEL_DEPLOY_MODE, "LOCAL")

    // Set input and output path
    conf.setBoolean(AngelConf.ANGEL_JOB_OUTPUT_PATH_DELETEONEXIST, true)
    conf.set(AngelConf.ANGEL_INPUTFORMAT_CLASS, classOf[CombineTextInputFormat].getName)
    conf.set(AngelConf.ANGEL_TRAIN_DATA_PATH, inputPath)
    conf.set(AngelConf.ANGEL_LOG_PATH, LOCAL_FS + TMP_PATH + "/LOG/rplog")
    conf.set(AngelConf.ANGEL_SAVE_MODEL_PATH, LOCAL_FS + TMP_PATH + "/out")
    conf.setInt(AngelConf.ANGEL_WORKER_MAX_ATTEMPTS, 1)

    // Set angel resource parameters #worker, #task, #PS
    conf.setInt(AngelConf.ANGEL_WORKERGROUP_NUMBER, 1)
    conf.setInt(AngelConf.ANGEL_WORKER_TASK_NUMBER, 1)
    conf.setInt(AngelConf.ANGEL_PS_NUMBER, 1)

    client = AngelClientFactory.get(conf)

    val vol = 663542l
    val V = 114729
    val k = 500
    val u = 110466
    val s = 113322
    val b = 1


    conf.setInt(nodes, V)
    conf.setLong(Vol, vol)
    conf.setInt(S,s)
    conf.setInt(K, k)
    conf.setInt(B, b)
    conf.setInt(U, u)
    conf.setInt(Feq, 1)
  }

  @Test
  def run(): Unit = {
    //start PS
    client.startPSServer()

    //init model
    val model = new AdniModel(conf)

    // Load model meta to client
    client.loadModel(model)

    // Start
    client.runTask(classOf[AdTrainTask])

    // Run user task and wait for completion, user task is set in "angel.task.user.task.class"
    client.waitForCompletion()

    // Save the trained model to HDFS
    //    client.saveModel(lDAModel)

    client.stop()
  }


}

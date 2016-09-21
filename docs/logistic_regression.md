LogisticRegression
=================
>  **[Logistic Regression](https://en.wikipedia.org/wiki/Logistic_regression)** is used to estimate the probability of a binary response based on one or more predictor (or independent) variables (features) .


```scala
import fregata.spark.data.LibSvmReader
import fregata.spark.metrics.classification.{AreaUnderRoc, Accuracy}
import fregata.spark.model.classification.{LogisticRegression}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Test LogisticRegression
 */
object TestLogisticRegression {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("logistic regression")
    val sc = new SparkContext(conf)
    val (_,trainData) = LibSvmReader.read(sc,"/Volumes/takun/libsvm/a9a",123)
    val (_,testData) = LibSvmReader.read(sc,"/Volumes/takun/libsvm/a9a.t",123)
    val model = LogisticRegression.run(trainData)
    val pd = model.predict(testData)
    val acc = Accuracy.of( pd.map{
      case (p,c,l) =>
        c -> l
    })
    println( s"Accuracy = $acc ")
    val auc = AreaUnderRoc.of( pd.map{
      case (p,c,l) =>
        p -> l
    })
    println( s"AreaUnderRoc = $auc ")
  }
}

```

    Accuracy = 0.8375506694509274
    AreaUnderRoc = 0.8927377914936719

SoftMax
=================
> **[SoftMax](https://en.wikipedia.org/wiki/Softmax_function)** is multi-class classification algorithm , generalization of **[Logistic Regression](https://en.wikipedia.org/wiki/Logistic_regression)** .


```scala
import fregata.spark.data.LibSvmReader
import fregata.preprocessing
import fregata.spark.metrics.classification.Accuracy
import fregata.spark.model.classification.SoftMax
import org.apache.spark.{SparkConf, SparkContext}

/**
 *  Test softmax
 */
object TestSoftMax {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("soft max")
    val sc = new SparkContext(conf)
    val (_,t1) = LibSvmReader.read(sc,"/Volumes/takun/libsvm/mnist2",780)
    val (_,t2) = LibSvmReader.read(sc,"/Volumes/takun/libsvm/mnist2.t",780)
    val k = 10
    val train = t1.map{
      case (x,label) => preprocessing.normalize(x) -> label
    }
    val testD = t2.map{
      case (x,label) => preprocessing.normalize(x) -> label
    }
    val model = SoftMax.run(k.toInt,train)
    val pd = model.predict(testD)
    val acc = Accuracy.of( pd.map{
      case (p,c,l) =>
        c -> l
    })
    println( s"Accuracy = $acc ")
  }
}

```

    Accuracy = 0.9075 

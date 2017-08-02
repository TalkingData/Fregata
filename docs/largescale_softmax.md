SoftMax for trillion dimensions
=================

Introduction
------
>Some times we have very high dimension data .But it is intractable to train the model.
So we develop a LR and Softmax for trillion dimensions feature .


Example
------------
> The following example demonstrate how the usage of Softmax . 
The argument `binSize=128` to control the sparse ratio of result model . 

```scala

  package fregata.spark
  
  import fregata._
  import fregata.spark.data.LibSvmReader
  import fregata.spark.metrics.classification.Accuracy
  import fregata.spark.model.largescale.SoftMax
  import org.apache.spark.{SparkConf, SparkContext}
  
  /**
   * Created by takun on 16/9/20.
   */
  object TestLargeScaleSoftMax {
  
    def main(args: Array[String]) {
      val conf = new SparkConf().setAppName("soft max").setMaster("local")
      val sc = new SparkContext(conf)
      val (_,t1) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/mnist2",780)
      val (_,t2) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/mnist2.t",780)
      val k = 10
      val train = t1.map{
        case (x,label) =>
          val sx = x.asInstanceOf[SparseVector]
          (sx.index.map( _.toLong ) , sx.data.map( _ / 255 ),label)
      }
      val testD = t2.map{
        case (x,label) =>
          val sx = x.asInstanceOf[SparseVector]
          (sx.index.map( _.toLong ) , sx.data.map( _ / 255 ),label)
      }
      val model = SoftMax.run(k.toInt,train,128,10)
      val pd = model.predict(testD)
      val acc = Accuracy.of( pd.map{
        case ((x,v,l),(ps,c)) =>
          asNum(c) -> l
      })
      val loss = fregata.spark.loss.log(pd.map{
        case ((x,v,l),(ps,c)) =>
          (l,asNum(c),ps(l.toInt))
      })
      println( s"Accuracy = $acc ")
      println( s"LogLoss = $loss ")
    }
  }


```
    Accuracy = 0.9104 
    LogLoss = 0.32324660716760434 

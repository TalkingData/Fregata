LR for trillion dimensions
=================

Introduction
------
>Some times we have very high dimension data .But it is intractable to train the model.
So we develop a LR and Softmax for trillion dimensions feature .


Example
------------
> The following example demonstrate how the usage of LR . 
The argument `binSize=32` to control the sparse ratio of result model . 

```scala

  import fregata.SparseVector
  import fregata.spark.data.LibSvmReader
  import fregata.spark.metrics.classification.{Accuracy, AreaUnderRoc}
  import fregata.spark.model.largescale.LogisticRegression
  import org.apache.spark.{SparkConf, SparkContext}
  
  /**
   * Created by takun on 16/9/20.
   */
  object TestLargeScaleLogisticRegression {
  
    def main(args: Array[String]): Unit = {
      val conf = new SparkConf().setAppName("logistic regression")
      val sc = new SparkContext(conf)
      val (_,trainData) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/a9a",123)
      val (_,testData) = LibSvmReader.read(sc,"/Volumes/takun/data/libsvm/a9a.t",123)
      val model = LogisticRegression.run(trainData.map{
        case (x,label) =>
          val sx = x.asInstanceOf[SparseVector]
          (sx.index.map( _.toLong ) , sx.data,label)
      },binSize = 32)
      val pd = model.predict(testData.map{
        case (x,label) =>
          val sx = x.asInstanceOf[SparseVector]
          (sx.index.map( _.toLong ) , sx.data,label)
      })
      val acc = Accuracy.of( pd.map{
        case ((x,v,l),(p,c)) =>
          c -> l
      })
      val auc = AreaUnderRoc.of( pd.map{
        case ((x,v,l),(p,c)) =>
          p -> l
      })
      val loss = fregata.spark.loss.log(pd.map{
        case ((x,v,l),(p,c)) =>
          if( l == 1d ) {
            (l,c,p)
          }else{
            ( l , c , 1-p )
          }
      })
      println( s"AreaUnderRoc = $auc ")
      println( s"Accuracy = $acc ")
      println( s"LogLoss = $loss ")
    }
  }

```

    AreaUnderRoc = 0.8993762522225006 
    Accuracy = 0.8485444048642673 
    LogLoss = 0.328876130227733 

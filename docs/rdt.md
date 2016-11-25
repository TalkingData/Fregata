RDT
=================
Introduction
-----------
> **Random Decision Trees** is a **Tree Based Ensemble** algorithm, the key points are:
> 
> - it can be used for Binary Classification, Multi-Class Classification and Regression(released soon)
> - it's very suitable to deal with large scale datas with low dimension
> - the feature of each node is selected randomly rather than calculating the metrics such as info_gain
> - the speed is very fast because of the ranom feature selection
> - it's only applicable for <font color=#0000ff>**Binary-Feature**</font>, so the value of each feature must be <font color=#0000ff>**0/1**</font> now
> - the maximum depth of each tree is 64
>


Example
------------

```scala

  def main(args: Array[String]) {
    val inTrain = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a"
    val inPredict = "/Users/hjliu/Projects/TalkingData/fregata/data/libsvm/a9a.t"
    val numTrees = 100
    val numFeatures = 123
    val numClasses = 2
    val depth = 32
    val minLeafCapacity = 10
    val maxPruneNum = 5

    val conf = new SparkConf().setAppName("rdt")
    val sc = new SparkContext(conf)
    val (_, trainData) = LibSvmReader.read(sc, inTrain, numFeatures.toInt)
    val (_, testData) = LibSvmReader.read(sc, inPredict, numFeatures.toInt)
    val rdt = new RDT(numTrees, numFeatures, numClasses, depth, minLeafCapacity, maxPruneNum)
    val model = rdt.train(trainData)

    val predicted = model.rdtPredict(testData)
    val auc = AreaUnderRoc.of(predicted.map {
      case ((x, l), (p, c)) =>
        p(1) -> l
    })

    val loss = fregata.spark.loss.log(predicted.map {
      case ((x, l), (ps, c)) =>
        (l, asNum(c), ps(l.toInt))
    })

    println(s"AreaUnderCurve : $auc ")
    println(s"LogLoss : $loss ")
  }

```
	AreaUnderCurve : 0.8635148469990402
	LogLoss:0.4531350015763193
Fregata: Machine Learning
==================================

[![GitHub license](http://og41w30k3.bkt.clouddn.com/apache2.svg)](./LICENSE)

- [Fregata](http://talkingdata.com) is a light weight, super fast, large scale machine learning library based on [Apache Spark](http://spark.apache.org/), and it provides high-level APIs in Scala.

- More accurate: For various problems, Fregata can achieve higher accuracy compared to MLLib.

- Higher speed: For Generalized Linear Model, Fregata often converges in one data epoch. For a 1 billion X 1 billion data set, Fregata can train a Generalized Linear Model in 1 minute with memory caching or 10 minutes without it. Usually, Fregata is 10-100 times faster than MLLib.

- Parameter Free: Fregata uses [GSA](http://arxiv.org/abs/1611.03608) SGD optimization, which dosen't require learning rate tuning, because we found a way to calculate appropriate learning rate in the training process. When confronted with super high-dimension problem, Fregata calculates remaining memory dynamically to determine the sparseness of the output, balancing accuracy and efficiency automatically. Both features enable Fregata to be treated as a standard module in data processing for different problems.

- Lighter weight: Fregata just uses Spark's standard API,  which allows it to be integrated into most business’ data processing flow on Spark quickly and seamlessly.

## Architecture
This documentation is about Fregata version 0.1

- core : mainly implements stand-alone algorithms based on GSA, including  **Classification** <font color=#808080> **Regression**</font> and <font color=#808080>  **Clustering** </font>
  - Classification: supports both binary and multiple classification
  - Regression: will release later
  - Clustering: will release later
- spark : mainly implements large scale machine learning algorithms based on **spark** by wrapping **core.jar** and supplies the corresponding algorithms

**Fregata supports spark 1.x and 2.x with scala 2.10 and scala 2.11 .**

## Algorithms
- [Trillion LR](./docs/largescale_lr.md)
- [Trillion SoftMax](./docs/largescale_softmax.md)
- [Logistic Regression](./docs/logistic_regression.md)
- [Combine Freatures Logistic Regression](./docs/clr.md)
- [SoftMax](./docs/softmax.md)
- [RDT](./docs/rdt.md)

## Installation

Two ways to get Fregata by Maven or SBT :

- Maven's pom.xml

```xml
    <dependency>
       <groupId>com.talkingdata.fregata</groupId>
        <artifactId>core</artifactId>
        <version>0.0.3</version>
    </dependency>
    <dependency>
        <groupId>com.talkingdata.fregata</groupId>
        <artifactId>spark</artifactId>
        <version>0.0.3</version>
    </dependency>
```

- SBT's build.sbt

```scala
    // if you deploy to local mvn repository please add
    // resolvers += Resolver.mavenLocal
    libraryDependencies += "com.talkingdata.fregata" % "core" % "0.0.3"
    libraryDependencies += "com.talkingdata.fregata" % "spark" % "0.0.3"
```

If you want to manual deploy to local maven repository , as follow :
```
git clone https://github.com/TalkingData/Fregata.git
cd Fregata
mvn clean package install
```

## Quick Start
Suppose that you're familiar with Spark, the example below shows how to use Fregata's **Logistic Regression**, and experimental datas can be obtained on [LIBSVM Data](https://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/)

- adding Fregata into project by Maven or SBT referring to the **Downloading** part
- importing packages

```scala
	import fregata.spark.data.LibSvmReader
	import fregata.spark.metrics.classification.{AreaUnderRoc, Accuracy}
	import fregata.spark.model.classification.LogisticRegression
	import org.apache.spark.{SparkConf, SparkContext}
```

- loading training datas by Fregata's LibSvmReader API

```scala
    val (_, trainData)  = LibSvmReader.read(sc, trainPath, numFeatures.toInt)
    val (_, testData)  = LibSvmReader.read(sc, testPath, numFeatures.toInt)
```

- building Logsitic Regression model by trainging datas

```scala
    val model = LogisticRegression.run(trainData)
```

- predicting the scores of instances

```scala
    val pd = model.classPredict(testData)
```

- evaluating the quality of predictions of the model by auc or other metrics

```scala
    val auc = AreaUnderRoc.of( pd.map{
      case ((x,l),(p,c)) =>
        p -> l
    })
```

## Input Data Format
Fregata's training API needs *RDD[(fregata.Vector, fregata.Num)]*, predicting API needs the same or *RDD[fregata.Vector]* without label

```scala
	import breeze.linalg.{Vector => BVector , SparseVector => BSparseVector , DenseVector => BDenseVector}
	import fregata.vector.{SparseVector => VSparseVector }

	package object fregata {
	  type Num = Double
	  type Vector = BVector[Num]
	  type SparseVector = BSparseVector[Num]
	  type SparseVector2 = VSparseVector[Num]
	  type DenseVector = BDenseVector[Num]
	  def zeros(n:Int) = BDenseVector.zeros[Num](n)
	  def norm(x:Vector) = breeze.linalg.norm(x,2.0)
	  def asNum(v:Double) : Num = v
	}

```

- if the data format is LibSvm, then *Fregata's LibSvmReader.read() API* can be used directly

```scala
	// sc is Spark Context
	// path is the location of input datas on HDFS
	// numFeatures is the number of features for single instance
	// minPartitions is the minimum number of partitions for the returned RDD pointing the input datas
	read(sc:SparkContext, path:String, numFeatures:Int=-1, minPartition:Int=-1):(Int, RDD[(fregata.Vector, fregata.Num)])
```

- else some constructions are needed

	- Using SparseVector

	```scala
		// indices is an 0-based Array and the index-th feature is not equal to zero
		// values  is an Array storing the corresponding value of indices
		// length  is the total features of each instance
		// label   is the instance's label

		// input datas with label
		sc.textFile(input).map{
			val indicies = ...
			val values   = ...
			val label    = ...
			...
			(new SparseVector(indices, values, length).asInstanceOf[Vector], asNum(label))
		}

		// input datas without label(just for predicting API)
		sc.textFile(input).map{
			val indicies = ...
			val values   = ...
			...
			new SparseVector(indices, values, length).asInstanceOf[Vector]
		}
	```
	- Using DenseVector

	```scala
		// datas is the value of each feature
		// label   is the instance's label

		// input datas with label
		sc.textFile(input).map{
			val datas = ...
			val label = ...
			...
			(new DenseVector(datas).asInstanceOf[Vector], asNum(label))
		}

		// input datas without label(just for predicting API)
		sc.textFile(input).map{
			val datas = ...
			...
			new DenseVector(indices, values, length).asInstanceOf[Vector]
		}
	```

## MailList:
   - yongjun.tian@tendcloud.com
   - haijun.liu@tendcloud.com
   - xiatian.zhang@tendcloud.com
   - fan.yao@tendcloud.com

## Contributors:

Contributed by [TalkingData](https://github.com/TalkingData/Fregata/contributors) .

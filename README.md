Fregata: Machine Learning
==================================

[![GitHub license](./img/apache2.svg)](./LICENSE)

- [Fregata](http://talkingdata.com) is a light weight, super fast, large scale machine learning library on Spark.
 
- More accurate: For varied problems, Fregata can achieve more accurate results compared to MLLib.
 
- High speed: For General Linear Model, Fregata often converges in one data epcho. For a 1 billion X 1 billion data set, Fregata can train a General Linear Model in 1 minute or 10 minutes with or without memory caching respectively. Usually, Fregata is 10-100 times faster than MLLib.
 
- Parameter Free: Fregata has optimized SGD solver, which dosen't need to tune lerarning rate, because we found out a way to calculate appropriate learning rate in the training process. When confronted with super large-scale-dimension problem, Fregata calculates remaining memory dynamically to determine the sparseness of the output, balancing accuracy and efficiency automatically. Both features enable Fregata to be treated as a standard module in data processing for different problems.
 
- Light weight: Fregata just use Spark's standard API,  which allows it to be integrated into most business’ data processing flow on Spark quickly and seamlessly.

#### We will release soon

## Algorithms

- [Logistic Regression](./docs/logistic_regression.md)
- [SoftMax](./docs/softmax.md)
- [Collaborative Filtering(ALS)](./)
- [KMeans/XMeans](./)

## Quick Start

```xml
    <dependency>
       <groupId>fregata</groupId>
        <artifactId>core</artifactId>
        <version>0.0.1</version>
    </dependency>
    <dependency>
        <groupId>fregata</groupId>
        <artifactId>spark</artifactId>
        <version>0.0.1</version>
    </dependency>
```

## Roadmap

- 2016-11-01 ：
  - Version 0.1 release
  - Publish paper on arxiv.org
  - Algorithms: Logistic Regression, Linear Regression, Softmax, CF(Funk-SVD)
 
- 2016-12-01：
  - Version 0.2 release
  - Use Alluxio to accelerate computing speed
  - Algorithms: RDT, RDH, K-Means, Logistic Model Tree
 
- 2017-01：
  - Version 0.3 release
  - Algorithms: SVM, X-Means

- 2017-02：
  - Version 0.4 release
  - Support Spark 2.x and DataFrame API.
 
- 2017-03：
  - Version 0.4 release
  - Algorithm: on-line Logistic Regression, Linear Regression, Softmax


## Contributors:

Contributed by [TalkingData](https://github.com/TalkingData/Fregata/contributors).

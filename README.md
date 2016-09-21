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

- [Logistic Regression](./)
- [SoftMax](./docs/softmax.md)
- [Collaborative Filtering(ALS)](./)
- [KMeans/XMeans](./)

## Quick Start

```
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

## More Information

- 


## Contributors:

Contributed by [TalkingData](https://github.com/TalkingData/Fregata/contributors).

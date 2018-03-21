package fregata.model.classification

//import breeze.io.TextWriter.FileWriter
import fregata._
import fregata.model.{Model, ModelTrainer}
import fregata.optimize.sgd.{AdaptiveSGD, StochasticGradientDescent}
import fregata.optimize.{Gradient, Target}
import fregata.param.ParameterServer
import fregata.util.VectorUtil
import java.io.{FileNotFoundException, FileWriter}

import scala.io.Source

/**
  * The greedy step averaging(GSA) method, a
parameter-free stochastic optimization algorithm for a variety of machine
learning problems. As a gradient-based optimization method, GSA makes use of
the information from the minimizer of a single sample's loss function, and
takes average strategy to calculate reasonable learning rate sequence. While
most existing gradient-based algorithms introduce an increasing number of
hyper parameters or try to make a trade-off between computational cost and
convergence rate, GSA avoids the manual tuning of learning rate and brings
in no more hyper parameters or extra cost. We perform exhaustive numerical
experiments for logistic and softmax regression to compare our method with
the other state of the art ones on 16 datasets. Results show that GSA is
robust on various scenarios.
  Please refer to the [[http://arxiv.org/abs/1611.03608]] for more details
 * Created by takun on 16/9/19.
 */
class LogisticGradient(ps:ParameterServer) extends Gradient {
  val thres = 0.95
  val update = Array(0.0)
  var stepSize = 0.0
  var i = 0.0
  def calculate(x:Vector,label:Num) : Array[Num] = {
    var weight = ps.get
    if( weight == null ) {
      ps.init(1,x.length + 1)
      weight = ps.get
    }
    val lambda = i / ( i + 1 )
    i += 1
    val margin = VectorUtil.wxpb(weight(0),x,1.0)
    val p1 = 1.0 / ( 1.0 + math.exp( - margin ) )
    val p0 = 1 - p1
    val b1 = math.exp(p1)
    val b0 = math.exp(p0)
    val x2 = math.pow(norm(x),2.0)
    // compute greedy step size
    val greedyStep = if( label == 1 ) {
      (p1 - thres) / ( thres * (1 - p0 * b0 - p1 * b1) + p1 * (1 - b0) ) / x2
    }else{
      (p0 - thres) / ( thres * (1 - p0 * b0 - p1 * b1 ) + p0 * (1 - b1)) / x2
    }
    // compute averaged step size
    stepSize = lambda * stepSize + (1 - lambda) * greedyStep
    update(0) = 2 * ( p1 - label ) * stepSize
    update
  }
}

class LogisticRegressionModel(val weights:Vector) extends ClassificationModel{

  var threshold = 0.5
  def setThreshold(t:Double) : this.type = {
    this.threshold = t
    this
  }
  def classPredict(x: Vector): (Num, Num) = {
    val margin = VectorUtil.wxpb(weights,x,1.0)
    val p = 1.0 / ( 1.0 + math.exp( - margin ) )
    val c = if( p > threshold ) 1.0 else 0.0
    (asNum(p),asNum(c))
  }
  override def saveModel(filename: String): Int = {
    val outFile = new FileWriter(filename, false)
    if (outFile == null) {
      -1
    } else {
      outFile.write(weights.size.toString + '\n')
      weights.toArray.foreach((x: Double) => outFile.write(x.toString + '\n'))
      outFile.flush()
      outFile.close()
      0
    }
  }
}

class LogisticRegression extends ModelTrainer {
  override type M = LogisticRegressionModel
  val ps = newPs
  val gradient = new LogisticGradient(ps)
  def buildModel(ps:ParameterServer) = new LogisticRegressionModel(ps.get(0))
  def run( data:Iterable[(Vector,Num)] ) = {
    val target = Target(gradient,ps)
    new AdaptiveSGD()
      .minimize(target)
      .run(data)
    new LogisticRegressionModel(ps.get(0))
  }

  override def loadModel(fn: String): Int = {
    var i = 0
    var ret = 0
    var last_weights: Array[Num] = null
    try {
      for (line <- Source.fromFile(fn).getLines()) {
        if (i == 0) last_weights = new Array[Num](line.toInt)
        else last_weights(i-1) = line.toFloat
        i += 1
      }
    } catch {
      case _: FileNotFoundException => { ret = -1 }
    }
    if (last_weights != null) ps.set(Array.fill(1){new DenseVector(last_weights)})
    ret
  }
}

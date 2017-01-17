package fregata.model.classification

import fregata._
import fregata.loss.LogLoss
import fregata.model.{ ModelTrainer}
import fregata.optimize.{Gradient, Target}
import fregata.optimize.sgd.AdaptiveSGD
import fregata.param.{ ParameterServer}
import fregata.util.VectorUtil

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
class SoftMaxGradient(ps:ParameterServer,k:Int) extends Gradient {
  val loss = new LogLoss
  var i = 0.0
  val thres = .95
  var stepSize = 0d
  def calculate(x:Vector,label:Num) : Array[Num] = {
    var weights = ps.get
    if( weights == null ) {
      ps.init(k,x.length + 1)
      weights = ps.get
    }
    val update = Array.ofDim[Num](weights.length)
    val lambda = i / (i+1)
    i += 1
    // compute greedy step size
    val (_ps,_) = SoftMaxModel.predict(weights,x)
    val yi = label.toInt
    val x2 = math.pow(norm(x),2.0)
    val pi = _ps(yi)
    // compute averaged step size
    val greedyStep = ( pi - thres ) / (thres * ( 1.0 - _ps.map( p => p * math.exp(p) ).sum ) + pi - math.E * pi / math.exp(pi) ) / x2
    stepSize = lambda * stepSize + (1-lambda) * greedyStep
    // compute update
    (0 until weights.length ).foreach { k =>
      val y = if( k == label ) asNum(1) else asNum(0)
      val gs = loss.gradient(x,asNum(_ps(k)),y)
      update(k) = asNum(gs * stepSize)
    }
    update
  }
}

class SoftMaxModel(k:Int,val weights:Array[Vector]) extends ClassificationModel {

  def this(ws:Array[Vector]) = this(ws.length,ws)

  /**
    * predict to get every class probability
    * @param x  input vector
    * @return
    */
  def softMaxPredict(x:Vector) : (Array[fregata.Num], Int) = {
    SoftMaxModel.predict(weights, x)
  }

  def softMaxPredict(data:S[(Vector,Num)]) : S[((Vector,Num),(Array[fregata.Num], Int))] = {
    data.map{
      case a @ (x,label) =>
        a -> softMaxPredict(x)
    }
  }

  /**
    * predict the max probability class
    * @param x input vector
    * @return (predict probability , predict class)
    */
  override def classPredict(x:Vector) = {
    val (probs, pLabel) = softMaxPredict(x)
    val p = probs(pLabel)
    if (k == 2 && pLabel == 0) {
      (asNum(1 - p), asNum(pLabel))
    } else {
      (asNum(p), asNum(pLabel))
    }
  }
}

object SoftMaxModel {

  private[classification] def predict(ws:Array[Vector],x:Vector) = {
    var maxI = 0
    var max = Double.NegativeInfinity
    var i = 0
    val margins = ws.map{
      w => VectorUtil.wxpb(w,x,1d)
    }
    val ps = ws.map {
      w =>
        val margin = margins(i)
        val sum = margins.map(m => math.exp( m - margin ) ).sum
        val p = 1.0 / sum
        if( p > max ) {
          max = p
          maxI = i
        }
        i += 1
        p
    }
    ( ps , maxI )
  }
}

class SoftMax(k:Int) extends ModelTrainer {
  override type M = SoftMaxModel
  val ps = newPs
  val gradient = new SoftMaxGradient(ps,k)
  def buildModel(ps:ParameterServer) = new SoftMaxModel(ps.get)
  def run( data:Iterable[(Vector,Num)] ) = {
    val target = Target(gradient,ps)
    new AdaptiveSGD()
      .minimize(target)
      .run(data)
    new SoftMaxModel(k,ps.get)
  }
}

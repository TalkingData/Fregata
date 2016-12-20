package fregata.model.classification

import fregata._
import fregata.model.{Model, ModelTrainer}
import fregata.optimize.Target
import fregata.param.ParameterServer

import scala.collection.mutable.ListBuffer

/**
  * Created by takun on 2016/10/24.
  */
class CLRModel(override val weights:Vector,val combines:Array[Array[Int]]) extends LogisticRegressionModel(weights) {

  def predict(x:Array[Vector]) : Num = clrPredict(x)._2

  def clrPredict(data:S[(Array[Vector],Num)]) : S[((Array[Vector],Num),(Num,Num))] = {
    val lengths = combines.map{
      comb => comb.map(i=>data.head._1(i).length).reduce( _ * _ )
    }
    val length = lengths.sum
    data.map{
      case a @ (x,label) =>
        val v = CLR.compactVector(x,combines,lengths,length)
        val r = classPredict(v)
        (a,r)
    }
  }

  def clrPredict(x: Array[Vector]): (Num, Num) = {
    val lengths = combines.map{
      comb => comb.zip(x).map( _._2.length ).reduce( _ * _ )
    }
    val length = lengths.sum
    val v = CLR.compactVector(x,combines,lengths,length)
    classPredict(v)
  }
}

object CLR {
  def compactVector(x:Array[Vector],combines:Array[Array[Int]],lengths:Array[Int],length:Int) = {
    var start = 0
    var i = 0
    val pairs = ListBuffer[(Int,Double)]()
    combines.foreach{
      comb =>
        val features = group(x,comb)
        pairs ++= indices_values(x,features,comb,start)
        start += lengths(i)
        i += 1
    }
    val (indices,values) = pairs.sortWith( _._1 < _._1 ).unzip
    new SparseVector(indices.toArray,values.toArray,length).asInstanceOf[Vector]
  }
  def indices_values(x:Array[Vector],features: ListBuffer[ListBuffer[(Int,Double)]],comb: Array[Int],start:Int) = {
    val pairs = Array.ofDim[(Int,Double)](features.length)
    var idx = 0
    features.foreach{
      fs =>
        var i = 0
        var index = start
        var block_size = 1
        var value = 1.0
        fs.foreach{
          case (k,v) =>
            if( i == 0) {
              index += k
            }else {
              index += k * block_size
            }
            block_size *= x(comb(i)).length
            value *= v
            i += 1
        }
        pairs(idx) = (index,value)
        idx += 1
    }
    pairs
  }

  def group(data:Array[Vector],indices:Array[Int]) = {
    val features = ListBuffer[ListBuffer[(Int,Double)]]()
    indices.foreach{
      i =>
        val v = data(i)
        if( features.size == 0 ) {
          v.activeIterator.foreach( features += ListBuffer(_) )
        } else {
          val tmp_fs = ListBuffer[ListBuffer[(Int,Double)]]()
          features.foreach{
            fs =>
              var j = 0
              val tmp = fs.clone()
              v.activeIterator.foreach{
                case a @ (k,v) =>
                  if( j == 0 ) {
                    fs += a
                  } else {
                    val tmp2 = tmp.clone()
                    tmp2 += a
                    tmp_fs += tmp2
                  }
                  j += 1
              }
          }
          features ++= tmp_fs
        }
    }
    features
  }
}
class CLR extends ModelTrainer{

  override type M = CLRModel
  val ps = newPs
  private[this] val gradient = new LogisticGradient(ps)
  private[this] val target = Target(gradient,ps)
  private[this] var combines :Array[Array[Int]] = _

  def buildModel(ps:ParameterServer) = new CLRModel(ps.get(0),combines)
  def run(data:Iterable[(Vector,Num)]) : CLRModel = {
    val d2 = data.map{case (x,label) => Array(x) -> label }
    run(d2,Array(Array(0)))
  }

  def run(data:Iterable[(Array[Vector],Num)],combines:Array[Array[Int]],iterationNum:Int,callback : (Model,Int) => Unit ) : CLRModel = {
    var model : CLRModel = null
    (0 until iterationNum).foreach{
      i =>
        val start = System.currentTimeMillis
        model = run(data,combines)
        if( callback != null ) callback(model,i)
        val end = System.currentTimeMillis
        println( end - start )
    }
    model
  }

  def run(data:Iterable[(Array[Vector],Num)],combines:Array[Array[Int]]) : CLRModel = {
    this.combines = combines
    val lengths = combines.map{
      comb => comb.map(i=>data.head._1(i).length).reduce( _ * _ )
    }
    val length = lengths.sum
    data.foreach{
      case (x,label) =>
        val vector = CLR.compactVector(x,combines,lengths , length)
        val gradients = target.gradient.calculate(vector,label)
        val delta = gradients
        target.ps.adjust(delta,vector)
    }
    new CLRModel(ps.get(0),combines)
  }

  /*def main(args: Array[String]): Unit = {
    run(Array(Array(
      new SparseVector(Array(0,1,2),Array(1.1,2.2,3.3),3) ,
      new SparseVector(Array(0,1,2),Array(4.4,5.5,6.6),3) ,
      new SparseVector(Array(0,1,2),Array(7.7,8.8,9.9),3)
    )),Array(Array(0),Array(1),Array(2),Array(0,2),Array(0,1),Array(1,2)))
  }*/
}

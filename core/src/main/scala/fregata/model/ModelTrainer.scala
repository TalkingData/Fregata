package fregata.model

import fregata._
import fregata.param.{LocalParameterServer, ParameterServer}

/**
 * Created by takun on 16/9/20.
 */
trait ModelTrainer extends Serializable{

  type M

  def newPs = ParameterServer.create
  def ps : ParameterServer
  def buildModel(ps:ParameterServer) : M

  def run(data:Iterable[(Vector,Num)],epochNum:Int ,callback : (M,Int) => Unit = null) : M = {
    var model : Any = null
    (0 until epochNum).foreach{
      i =>
        model = run(data)
        if( callback != null ) callback(model.asInstanceOf[M],i)
    }
    model.asInstanceOf[M]
  }

  def run(data:Iterable[(Vector,Num)]) : M

  def loadModel(fn: String): Int = 0
}

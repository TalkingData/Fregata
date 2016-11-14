package fregata

/**
  * Created by takun on 2016/10/12.
  */
package object loss {
  def log(data:Iterable[(Num,Num,Num)]) = new LogLoss().of(data)
  def rmse(data:Iterable[(Num,Num,Num)]) = new RMSE().of(data)
}

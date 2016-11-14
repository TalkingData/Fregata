package fregata.model

import fregata._

/**
 * Created by takun on 16/9/20.
 */
trait Model extends Serializable {

  type S[?] = Iterable[?]
  /**
   * @param x input vector
   * @return predict value
   */
  def predict(x:Vector) : Num
  def predict(data:S[Vector]) : S[(Vector,Num)] = data.map{
    x => (x , predict(x))
  }
}

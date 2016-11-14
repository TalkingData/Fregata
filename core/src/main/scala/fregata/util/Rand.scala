package fregata.util

import com.github.fommil.netlib.BLAS.{getInstance => blas}
import fregata._
import breeze.stats.distributions.{Rand => BRand }

/**
 * Created by takun on 16/9/20.
 */
object Rand {

  def gaussian = BRand.gaussian.get
  def gaussian(n:Int) = {
    val r = BRand.gaussian
    val data = Array.ofDim[Float](n)
    (0 until n).foreach{
      i =>
        data(i) = r.get().toFloat
        val nrm = blas.snrm2(n,data,1)
        blas.sscal(n, 1.0f / nrm, data, 1)
    }
    new DenseVector(data.map(d=>asNum(d)))
  }
}

package adni

import adni.psf.FloatPartCSRResult
import adni.utils.AtomicFloat
import structures.CSRMatrix

import scala.collection.mutable

/**
  * Created by chris on 11/2/17.
  */
class AdOperator(csrMatrix:CSRMatrix[Float],model:AdniModel) {
  val mVec: mutable.Map[Int,Float] = mutable.Map[Int,Float]()

  def multiply(csr:FloatPartCSRResult,
               result:Array[AtomicFloat],
               original:Array[Float],
               biject:Map[Int,Int]) = {
    csr.read(mVec)
    mVec.foreach{case(k, v) =>
        biject.get(k) match {
          case Some(pos) => original(pos) = v
          case None => 0
        }
    }

    (0 until csrMatrix.numOfRows) foreach {i =>
      result(i).addAndGet(csrMatrix.dotRow(mVec, i))
    }
    mVec.clear()
  }
}

package fregata.hash

import fregata._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by hjliu on 16/11/29.
  */
class SimHash extends Serializable{

  var featureSize: Int = 0
  var hashbits: Int = 0
  var hashfunc: (Int) => Array[Long] = x => null

  def this(featureSize: Int, hashBits: Int)= {

    this
    this.featureSize = featureSize
    this.hashbits = hashBits

    hashBits match {
      case 64 => hashfunc = hash64
      case 128 => hashfunc = hash128
      case 192 => hashfunc = hash192
      case 256 => hashfunc = hash256
      case 320 => hashfunc = hash320
      case 384 => hashfunc = hash384
      case 448 => hashfunc = hash448
      case 512 => hashfunc = hash512
      case _ => hashfunc = hash256
    }

  }

  def hash64(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](1)
    t(0) = h
    t
  }

  def hash128(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](2)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h
    t
  }

  def hash192(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](3)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h
    t
  }

  def hash256(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](4)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h

    h = -in.toLong - featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(3) = h
    t
  }

  def hash320(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](5)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h

    h = -in.toLong - featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(3) = h

    h = in.toLong + 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(4) = h

    t
  }

  def hash384(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](6)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h

    h = -in.toLong - featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(3) = h

    h = in.toLong + 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(4) = h

    h = -in.toLong - 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(5) = h

    t
  }

  def hash448(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](7)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h

    h = -in.toLong - featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(3) = h

    h = in.toLong + 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(4) = h

    h = -in.toLong - 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(5) = h

    h = in.toLong + 3 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(6) = h

    t
  }

  def hash512(in: Int): Array[Long] = {
    var h = in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47

    val t = new Array[Long](8)
    t(0) = h

    h = -in.toLong
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(1) = h

    h = in.toLong + featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(2) = h

    h = -in.toLong - featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(3) = h

    h = in.toLong + 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(4) = h

    h = -in.toLong - 2 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(5) = h

    h = in.toLong + 3 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(6) = h

    h = -in.toLong - 3 * featureSize
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    t(7) = h
    t
  }

  def hash(vs: Vector) = {
    val s = new Array[Num](hashbits)
    val one = 1L

    vs.foreachPair {
      (i, v) =>
        if (v != 0d) {
          val h = hashfunc(i)
          var k = 0
          val hlen = h.length
          while (k < hlen) {
            var l = 0
            while (l < 64) {
              if ((h(k) & (one << l)) != 0) {
                s(k * 64 + l) += v
              } else {
                s(k * 64 + l) -= v
              }
              l += 1
            }
            k += 1
          }
        }
    }

    var j = 0
    val indices = ArrayBuffer[Int]()
    val values  = ArrayBuffer[Num]()
    while (j < s.length) {
      if (s(j) >= 0) {
        indices.append(j)
        values.append(1d)
      }
      j += 1
    }
    new SparseVector(indices.toArray, values.toArray, hashbits).asInstanceOf[Vector]
  }
}

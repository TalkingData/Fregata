package fregata.hash

/**
  * Created by hjliu on 16/11/25.
  */
class FastHash extends Hash {

  def getHash(in:Long):Int = {
    var h = in
    h ^= (h >> 23)
    h *= 0x2127599bf4325c37L
    h ^= h >> 47
    math.abs(h.toInt)
  }

}

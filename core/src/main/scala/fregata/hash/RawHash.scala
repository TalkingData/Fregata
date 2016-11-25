package fregata.hash

/**
  * Created by hjliu on 16/11/22.
  */
class RawHash extends Hash{

  def getHash(input: Long) :Int = {
    var key = input
    val c2 = 0x27d4eb2d
    key = (key ^ 61) ^ (key >>> 16)
    key = key + (key << 3)
    key = key ^ (key >>> 4)
    key = key * c2
    key = key ^ (key >>> 15)
    math.abs(key.toInt)
  }

}

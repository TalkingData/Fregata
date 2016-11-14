package fregata.data

import fregata._

import scala.io.Source

/**
 * Created by takun on 16/7/30.
 */
object LibSvmReader {

  def mapLine(l:String) = {
    val line = l.trim
    if( line.isEmpty || line.startsWith("#") ) None
    else {
      val items = line.split(" ")
      val label = items.head.toDouble
      val (indices, values) = items.tail.filter(_.nonEmpty).map { item =>
        val indexAndValue = item.split(':')
        val index = indexAndValue(0).toInt - 1 // Convert 1-based indices to 0-based.
      val value = asNum(indexAndValue(1).toDouble)
        (index, value)
      }.unzip

      // check if indices are one-based and in ascending order
      var previous = -1
      var i = 0
      val indicesLength = indices.length
      while (i < indicesLength) {
        val current = indices(i)
        require(current > previous, "indices should be one-based and in ascending order" )
        previous = current
        i += 1
      }
      if( label == -1 ) {
        Some( (0d, indices.toArray, values.toArray) )
      }else{
        Some( (label, indices.toArray, values.toArray) )
      }
    }
  }

  def read(path:String,numFeatures : Int = -1 ) = {
    val source = Source.fromFile(path)
    try{
      val parsed = source.getLines().flatMap{
        l => mapLine(l)
      }.toArray
      val n = if( numFeatures <= 0 ) {
        parsed.map { case (label, indices, values) =>
          indices.lastOption.getOrElse(0)
        }.max + 1
      } else numFeatures
      (n,parsed.map{
        case (label,indices,values) =>
          new SparseVector(indices,values,n).asInstanceOf[Vector] -> asNum(label)
      })
    } finally {
      source.close
    }
  }
}

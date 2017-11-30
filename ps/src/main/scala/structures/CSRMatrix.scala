package structures

import scala.collection.{Map, mutable}
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
  * Created by chris on 10/30/17.
  */
/*
the csr matrix only support sum/dot with vector/elementwise add and multiplication (all numeric type)
*/

class CSRMatrix[T:ClassTag](val values:Array[T],val rows:Array[Int], val columns:Array[Int], shape:(Int,Int))(implicit num: Numeric[T]) {
  val numOfValues: Int = values.length
  val (numOfRows, numOfColumns) = shape
  val rowLens: Array[Int] = Array.ofDim[Int](numOfRows)
  val offSet: Array[Int] = Array.ofDim[Int](numOfRows + 1)
  (0 until numOfValues) foreach{i =>
    rowLens(rows(i)) += 1
  }
  offSet(0)
  (0 until numOfRows) foreach {i=>
    offSet(i+1) = offSet(i) + rowLens(i)
  }

  def sum():T = {
    values.sum
  }

  def sumRow(rowId:Int):T = {
    var sum:T = num.fromInt(0)
    (offSet(rowId) until offSet(rowId + 1)) foreach{i =>
      sum = num.plus(sum,values(i))
    }
    sum
  }

  def sum(axis:Int) :Array[T] = {
      var sums: Array[T] = null
      if (axis == 0) {
        sums = Array.ofDim[T](numOfRows)
        (0 until numOfRows) foreach { i =>
          sums(i) = values.slice(offSet(i), offSet(i + 1)).sum
        }
      } else if (axis == 1) {
        sums = Array.ofDim[T](numOfColumns)
        (0 until numOfValues) foreach { i =>
          sums(columns(i)) = num.plus(sums(columns(i)), values(i))
        }
      } else {
        throw new Exception("The CSRMatrix is only a 2d array")
      }
      sums
  }

  def dot[Z:ClassTag](vector: Array[Z])(implicit ev: T => Z, num:Numeric[Z]): Array[Z] = {
    val dotProduct:Array[Z] = Array.ofDim[Z](numOfRows)
    if(vector.length == numOfColumns) {
      (0 until numOfRows) foreach{ i =>
        (offSet(i) until offSet(i + 1)) foreach{ z =>
          val prod = num.times(vector(columns(z)),values(z))
          dotProduct(i) = num.plus(dotProduct(i), prod)
        }
      }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dotRow[Z:ClassTag](vector: Array[Z],rowNum:Int)(implicit ev: T => Z, num:Numeric[Z]): Z = {
    var dotProduct:Z = num.fromInt(0)
    if(vector.length == numOfColumns) {
        (offSet(rowNum) until offSet(rowNum + 1)) foreach{ z =>
          val prod = num.times(vector(columns(z)),values(z))
          dotProduct = num.plus(dotProduct, prod)
        }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dot1[Z:ClassTag](vector: Array[Z])(implicit ev: Z => T): Array[T] = {
    val dotProduct:Array[T] = Array.ofDim[T](numOfRows)
    if(vector.length == numOfColumns) {
      (0 until numOfRows) foreach{ i =>
        (offSet(i) until offSet(i + 1)) foreach{ z =>
          val prod = num.times(vector(columns(z)),values(z))
          dotProduct(i) = num.plus(dotProduct(i), prod)
        }
      }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dotRow1[Z:ClassTag](vector: Array[Z],rowNum:Int)(implicit ev: Z => T): T = {
    var dotProduct: T = num.fromInt(0)
    if(vector.length == numOfColumns) {
        (offSet(rowNum) until offSet(rowNum + 1)) foreach{ z =>
          val prod = num.times(vector(columns(z)),values(z))
          dotProduct = num.plus(dotProduct, prod)
        }

    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }


  def dot[Z:ClassTag](vector:Map[Int, Z])(implicit ev: T => Z, num:Numeric[Z]): Array[Z] = {
    val dotProduct:Array[Z] = Array.ofDim[Z](numOfRows)
    if(vector.size <= numOfColumns) {
      (0 until numOfRows) foreach{ i =>
        (offSet(i) until offSet(i + 1)) foreach{ z =>
          vector.get(columns(z)) match {
            case Some(v) => {
              val prod = num.times(v, values(z))
              dotProduct(i) = num.plus(dotProduct(i), prod)
            }
            case None => 0
          }

        }
      }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dotRow[Z:ClassTag](vector:Map[Int, Z], rowNum:Int)(implicit ev: T => Z, num:Numeric[Z]): Z = {
    var dotProduct:Z = num.fromInt(0)
    if(vector.size <= numOfColumns) {
        (offSet(rowNum) until offSet(rowNum + 1)) foreach{ z =>
          vector.get(columns(z)) match {
            case Some(v) => {
              val prod = num.times(v, values(z))
              dotProduct = num.plus(dotProduct, prod)
            }
            case None => 0
          }

        }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dot1[Z:ClassTag](vector:Map[Int, Z])(implicit ev: Z => T): Array[T] = {
    val dotProduct:Array[T] = Array.ofDim[T](numOfRows)
    if(vector.size <= numOfColumns) {
      (0 until numOfRows) foreach{ i =>
        (offSet(i) until offSet(i + 1)) foreach{ z =>
          vector.get(columns(z)) match {
            case Some(v) => {
              val prod = num.times(v, values(z))
              dotProduct(i) = num.plus(dotProduct(i), prod)
            }
            case None => 0
          }

        }
      }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def dotRow1[Z:ClassTag](vector:Map[Int, Z],rowNum:Int)(implicit ev: Z => T): T = {
    var dotProduct:T = num.fromInt(0)
    if(vector.size <= numOfColumns) {
        (offSet(rowNum) until offSet(rowNum + 1)) foreach{ z =>
          vector.get(columns(z)) match {
            case Some(v) => {
              val prod = num.times(v, values(z))
              dotProduct = num.plus(dotProduct, prod)
            }
            case None => 0
          }
        }
    } else {
      throw new Exception("shape doesn't match")
    }
    dotProduct
  }

  def multiply[Z:ClassTag](csrMatrix: CSRMatrix[Z])(implicit ev: T => Z, num:Numeric[Z]): CSRMatrix[Z] = {
    val values = ArrayBuffer[Z]()
    val columns = ArrayBuffer[Int]()
    val rows = ArrayBuffer[Int]()
    (0 until numOfRows) foreach { i =>
      val map = mutable.Map[Int,Z]()
      (offSet(i) until offSet(i + 1)) foreach { z =>
        map.+= (this.columns(z) -> this.values(z))
      }
      (csrMatrix.offSet(i) until csrMatrix.offSet(i + 1)) foreach{ z =>
        val col = csrMatrix.columns(z)
        map.get(col) match {
          case Some(v) => map += (col -> num.times(csrMatrix.values(z), this.values(i)))
          case None => map.-=(col)
        }
      }
      map.foreach{case (k ,v) =>
        rows.append(i)
        columns.append(k)
        values.append(v)
      }
    }
    new CSRMatrix(values.toArray,rows.toArray,columns.toArray,this.shape)
  }

  def multiply1[Z:ClassTag](csrMatrix: CSRMatrix[Z])(implicit ev: Z => T): CSRMatrix[T] = {
    val values = ArrayBuffer[T]()
    val columns = ArrayBuffer[Int]()
    val rows = ArrayBuffer[Int]()
    (0 until numOfRows) foreach { i =>
      val map = mutable.Map[Int,T]()
      (offSet(i) until offSet(i + 1)) foreach { z =>
        map.+= (this.columns(z) -> this.values(z))
      }
      (csrMatrix.offSet(i) until csrMatrix.offSet(i + 1)) foreach{ z =>
        val col = csrMatrix.columns(z)
        map.get(col) match {
          case Some(v) => map += (col -> num.times(csrMatrix.values(z), this.values(i)))
          case None => map.-=(col)
        }
      }
      map.foreach{case (k ,v) =>
        rows.append(i)
        columns.append(k)
        values.append(v)
      }
    }
    new CSRMatrix(values.toArray,rows.toArray,columns.toArray,this.shape)
  }

  def add[Z:ClassTag](csrMatrix: CSRMatrix[Z])(implicit ev: T => Z, num:Numeric[Z]): CSRMatrix[Z] = {
    val values = ArrayBuffer[Z]()
    val columns = ArrayBuffer[Int]()
    val rows = ArrayBuffer[Int]()
    (0 until numOfRows) foreach { i =>
      val map = mutable.Map[Int,Z]()
      (offSet(i) until offSet(i + 1)) foreach { z =>
        map.+= (this.columns(z) -> this.values(z))
      }
      (csrMatrix.offSet(i) until csrMatrix.offSet(i + 1)) foreach{ z =>
        val col = csrMatrix.columns(z)
        map.get(col) match {
          case Some(v) => map += (col -> num.plus(csrMatrix.values(z), this.values(i)))
          case None => map += (col -> csrMatrix.values(z))
        }
      }
      map.foreach{case (k ,v) =>
        rows.append(i)
        columns.append(k)
        values.append(v)
      }
    }
    new CSRMatrix(values.toArray,rows.toArray,columns.toArray,this.shape)
  }

  def add1[Z:ClassTag](csrMatrix: CSRMatrix[Z])(implicit ev: Z => T): CSRMatrix[T] = {
    val values = ArrayBuffer[T]()
    val columns = ArrayBuffer[Int]()
    val rows = ArrayBuffer[Int]()
    (0 until numOfRows) foreach { i =>
      val map = mutable.Map[Int,T]()
      (offSet(i) until offSet(i + 1)) foreach { z =>
        map.+= (this.columns(z) -> this.values(z))
      }
      (csrMatrix.offSet(i) until csrMatrix.offSet(i + 1)) foreach{ z =>
        val col = csrMatrix.columns(z)
        map.get(col) match {
          case Some(v) => map += (col -> num.plus(csrMatrix.values(z), this.values(i)))
          case None => map += (col -> csrMatrix.values(z))
        }
      }
      map.foreach{case (k ,v) =>
        rows.append(i)
        columns.append(k)
        values.append(v)
      }
    }
    new CSRMatrix(values.toArray,rows.toArray,columns.toArray,this.shape)
  }

}

object CSRMatrix{
  def main(args: Array[String]): Unit = {
    val values = Array(0f,1f,2f,3f)
    val float = Array(0f,1f,2f,3f)
    val rows = Array(0,1,2,3)
    val columns = Array(0,1,2,3)
    val shape = (4,4)
    val cSRMatrix = new CSRMatrix(values,rows,columns,shape)
    println(cSRMatrix.sumRow(1))
  }
}
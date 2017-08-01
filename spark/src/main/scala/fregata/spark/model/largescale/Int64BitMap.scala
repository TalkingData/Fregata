package fregata.spark.model.largescale

import java.io._

import org.roaringbitmap.RoaringBitmap

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer
;

/**
  * Created by takun on 15/10/8.
  */
class Int64BitMap extends Serializable{

  type B = RoaringBitmap

  val MAX_VALUE = Integer.MAX_VALUE

  private val offsets = ArrayBuffer[Long]()

  // [ high -> low ]
  var bitmaps = ArrayBuffer[RoaringBitmap]()

  def this(bs : ArrayBuffer[RoaringBitmap]) = {
    this()
    bitmaps = bs
  }

  def empty = new RoaringBitmap

  private def border(idx:Int) = {
    if( idx >= offsets.size ) {
      (offsets.size to idx).foreach{
        i => offsets += i * MAX_VALUE.toLong
      }
    }
    offsets(idx)
  }

  private def add(value:Int,idx:Int) : this.type = {
    if( bitmaps.size <= idx ) {
      (bitmaps.size to idx).foreach {
        i =>
          bitmaps += empty
      }
    }
    bitmaps(idx).add(value)
    this
  }

  def add(value:Int) : this.type = {
    if( value > MAX_VALUE ) add(value.toLong) else add(value,0)
  }
  def add(value:Long) : this.type = {
    val (v,idx) = split(value)
    add(v,idx)
  }

  def contains(value:Int,idx:Int) : Boolean = bitmaps(idx).contains(value)
  def contains(value:Int) : Boolean = bitmaps(0).contains(value)
  def contains(value:Long) : Boolean = {
    val (v,idx) = split(value)
    if( idx >= bitmaps.length ) false else bitmaps(idx).contains(v)
  }

  private def zip(eb : Int64BitMap) = {
    val max = Math.max(eb.bitmaps.size,bitmaps.size)
    val tmp = ArrayBuffer[(B,B)]()
    (0 until max).foreach{
      i =>
        val b1 = if(i >= bitmaps.size) empty else bitmaps(i)
        val b2 = if(i >= eb.bitmaps.size) empty else eb.bitmaps(i)
        tmp += b1 -> b2
    }
    tmp
  }

  def op(f : (B,B) => Unit )(eb:Int64BitMap) : Int64BitMap = {
    this.bitmaps = zip(eb).map{
      case (a,b) =>
        f(a,b)
        a
    }
    this
  }

  def or(eb : Int64BitMap) = op( _ or _ )(eb)
  def and(eb : Int64BitMap) = op( _ and _ )(eb)
  def xor(eb : Int64BitMap) = op( _ xor _ )(eb)
  def cardinality = bitmaps.map( _.getCardinality.toLong ).sum

  def iterator[T]( value : (Int,Int,Long) => T ) =
  {
    val iter = bitmaps.iterator
    var idx = -1
    new Iterator[T] {
      var it: java.util.Iterator[java.lang.Integer] = null
      def hasNext = {
        while ( (it == null || !it.hasNext) && iter.hasNext) {
          val _it = iter.next.iterator
          idx += 1
          if( _it.hasNext ) it = _it
        }
        it != null && it.hasNext
      }
      def next = value(it.next , idx, border(idx) )

    }
  }

  def intIterator : Iterator[Int] = iterator( (next,idx,offset) => next + offset.toInt )
  def longIterator : Iterator[Long] = iterator( (next,idx,offset) => next + offset )
  def javaIntIterator : java.util.Iterator[Int] = JavaConverters.asJavaIteratorConverter(intIterator).asJava
  def javaLongIterator : java.util.Iterator[Long] = JavaConverters.asJavaIteratorConverter(longIterator).asJava

  override def clone = {
    new Int64BitMap( bitmaps.map( _.clone ) )
  }

  def serialize(out: DataOutput) = {
    val size = bitmaps.size - 1
    bitmaps.zipWithIndex.foreach{
      case (bitmap,i) =>
        bitmap.serialize(out)
        if( i < size ) out.writeByte(i)
    }
  }

  def deserialize(in: DataInput) = {
    var hasNext = true
    while( hasNext ) {
      val bitmap = empty
      bitmap.deserialize(in)
      bitmaps += bitmap
      hasNext = try{
        in.readByte() != -1
      }catch {
        case e : EOFException => false
      }
    }
  }

  def toBytes = {
    val bos = new ByteArrayOutputStream
    serialize(new DataOutputStream(bos))
    bos.toByteArray
  }

  def fromBytes(bytes:Array[Byte]): this.type = {
    val bis = new ByteArrayInputStream(bytes)
    deserialize(new DataInputStream(bis))
    this
  }

  private def split(value:Long) = ( (value % MAX_VALUE).toInt , (value / MAX_VALUE).toInt )

 }
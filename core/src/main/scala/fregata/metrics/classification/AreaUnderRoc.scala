package fregata.metrics.classification

import java.util.Random
import fregata._

/**
 * Created by takun on 16/6/1.
 */
object AreaUnderRoc {

  def of(it:Iterable[(Num, Num)]):Double = {

    val rs = it.toArray
    val rd = new Random(System.currentTimeMillis())

    for(i <-0 until rs.length){
      rs(i) = (rs(i)._1 + rd.nextFloat()*0.00001f, rs(i)._2)
    }

    scala.util.Sorting.quickSort(rs)(scala.math.Ordering.by[(Double, Double), Double](_._1))

    if(rs(0)._1 == rs(rs.length-1)._1){
      return 0.5;
    }


    val ranks = new Array[Double](rs.length);
    var M:Long = 0;
    var c = 0;

    for(i <- 0 until rs.length){
      ranks(i) = i+1;
      if(rs(i)._2==1.0f){
        M += 1;
      }
    }

    if(M==ranks.length||M==0){
      return 0.5;
    }

    var sum:Double = ranks(0);

    c = 1;
    var s = -1;
    for(i <- 1 until rs.length){
      if(rs(i-1)._1==rs(i)._1){
        if(s == -1){
          s = i-1;
        }
        sum += ranks(i);
        c += 1;
      }else if(c>1){
        for(j <- s until (s+c)){
          ranks(j) = sum / c;
        }
        c = 1;
        s = -1;
        sum = ranks(i);
      }else{
        sum = ranks(i);
      }
    }
    var pRankNum:Double = 0.0;
    for(i <- 0 until ranks.length){
      if(rs(i)._2 == 1.0f){
        pRankNum += ranks(i);
      }
    }

    pRankNum -= M*(M+1)/2

    return pRankNum/(M*(ranks.length-M))
  }
}

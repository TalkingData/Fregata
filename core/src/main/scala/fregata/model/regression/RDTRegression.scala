package fregata.model.regression

import java.util.Random

import fregata._
import fregata.model.classification.RDT

import scala.collection.mutable.{HashMap => MHashMap, ArrayBuffer}

/**
  * Created by hjliu on 16/11/28.
  */

class RDTRegressionModel(depth: Int, seeds: Array[Int], trees: Array[MHashMap[Long, Int]],
                         models: MHashMap[(Int, (Long, Byte)), (Num, Int)]) extends RegressionModel {

  def getPath(inst: fregata.Vector, tree: MHashMap[Long, Int]) = {
    var path = 0l
    var node = 1l
    var bCovered = true
    var i = 0
    while (i < depth - 1 && bCovered) {
      tree.get(node) match {
        case Some(feature) =>
          val xi = if (0d != inst(feature)) 1l else 0l
          path |= xi << (depth - 2 - i).toLong
          node = node * 2 + xi
        case _ =>
          path = 0
          bCovered = false
      }
      i += 1
    }
    (path, (depth - 1).toByte)
  }

  def getY(seed: Int, rawPath: (Long, Byte)) = {
    var y = 0d
    var n = 0
    var i = 0
    var bFound = false
    while (i < rawPath._2 && !bFound) {
      models.get(seed ->(rawPath._1 << i, (rawPath._2 - i).toByte)) match {
        case Some(yn) =>
          bFound = true
          y = yn._1
          n = yn._2
        case _ =>
      }
      i += 1
    }
    (y, n)
  }

  override def predict(x: Vector) = {
    var y = 0d
    var n = 0
    var j = 0
    while (j < trees.length) {
      val rawPath = getPath(x, trees(j))
      val (y_, n_) = getY(seeds(j), rawPath)
      y += y_
      n += n_
      j += 1
    }
    if (n != 0) y/n else n
  }
}

class RDTRegression(numTrees: Int, depth: Int, numFeatures: Int, seed: Long = 20170315l)
  extends RDT(numTrees, depth, numFeatures, seed) {
  private var models = MHashMap[(Int, (Long, Byte)), (Num, Int)]()

  def setModels(models: MHashMap[(Int, (Long, Byte)), (Num, Int)]) = {
    this.models = models
  }

  def getModels = models

  def trainModels(s: Int, y_ : Num, pathDepth: (Long, Byte)) = {
    models.getOrElse((s, pathDepth), (0d, 0)) match {
      case (y, n) =>
        models.update((s, pathDepth), (y_ + y, n+1))
    }
  }

  def train(insts: Array[(fregata.Vector, fregata.Num)]) = {
    super.train(insts, trainModels)
    new RDTRegressionModel(depth, seeds.toArray, trees, models)
  }

  def pruneModels[T](yn_input : T, minLeafCapacity: Int, path_ :Long, depth_ : Byte,
                     s:Int, newModels_ : MHashMap[(Int, (Long, Byte)), T],
                     abRemove:ArrayBuffer[(Int, (Long, Byte))]) =
  {
    val yn = yn_input.asInstanceOf[(Num, Int)]
    var bPruneNeeded = false
    val newModels = newModels_.asInstanceOf[MHashMap[(Int, (Long, Byte)), (Num, Int)]]
    if (yn._2 < minLeafCapacity) {
      val shortPath = (path_ >> 1, (depth_ - 1).toByte)
      newModels.getOrElse((s, shortPath), (0d, 0)) match {
        case yn_ =>
          newModels.update((s, shortPath), (yn._1 + yn_._1, yn._2 + yn_._2))
          abRemove.append((s, (path_, depth_)))
      }
      bPruneNeeded = true
    }
    bPruneNeeded
  }

  def prune(minLeafCapacity: Int, maxPruneNum: Int = 1): RDTRegressionModel = {
    super.prune[(Num, Int)](minLeafCapacity, maxPruneNum, models, pruneModels)
    new RDTRegressionModel(depth, seeds.toArray, trees, models)
  }
}

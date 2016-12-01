package fregata.model.classification

import java.util.Random

import fregata._
import fregata.hash.{FastHash, Hash}

import scala.collection.mutable.{HashMap => MHashMap, HashSet => MHashSet, ArrayBuffer}

/**
  * Created by hjliu on 16/10/31.
  */

class RDTModel(depth: Int, numClasses: Int, seeds: Array[Int], trees: Array[MHashMap[Long, Int]],
               models: MHashMap[(Int, (Long, Byte)), Array[Int]]) extends ClassificationModel {

  def rdtPredict(x: Vector): (Array[Double], Int) = {
    val count = Array.ofDim[Int](numClasses)
    var j = 0
    while (j < trees.length) {
      val rawPath = getPath(x, trees(j))
      val count_ = getCount(seeds(j), rawPath)
      (0 until numClasses).foreach(i => count(i) += count_(i))
      j += 1
    }

    j = 0
    var pLabel = 0
    var max = 0d
    val total = count.sum
    val probs = Array.ofDim[Num](numClasses)
    while (j < numClasses) {
      if (count(j) > max) {
        max = count(j)
        pLabel = j
      }
      probs(j) = asNum(count(j) + 1) / (total + numClasses)
      j += 1
    }

    probs -> pLabel
  }


  def rdtPredict(data: S[(Vector, Num)]): S[((Vector, Num), (Array[fregata.Num], Int))] = {
    data.map {
      case a@(x, label) =>
        a -> rdtPredict(x)
    }
  }

  def classPredict(x: Vector): (Num, Num) = {
    val (probs, pl) = rdtPredict(x)

    if (numClasses == 2 && pl == 0)
      (probs(1), asNum(pl))
    else
      (asNum(probs(pl)), asNum(pl))
  }

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

  def getCount(seed: Int, rawPath: (Long, Byte)) = {
    var bFound = false
    var count = Array.ofDim[Int](numClasses)
    var i = 0
    while (i < rawPath._2 && !bFound) {
      models.get(seed ->(rawPath._1 << i, (rawPath._2 - i).toByte)) match {
        case Some(c) =>
          bFound = true
          count = c
        case _ =>
      }
      i += 1
    }
    count
  }
}

class RDT(numTrees: Int, depth: Int, numFeatures: Int, seed: Long = 20170315l)
  extends Serializable {
  var hasher: Hash = new FastHash

  var trees = Array.ofDim[MHashMap[Long, Int]](numTrees)
  var seeds = ArrayBuffer[Int]()

  def setTrees(trees: Array[MHashMap[Long, Int]]) = {
    this.trees = trees
  }

  def setHash(h: Hash) = {
    this.hasher = h
  }

  def getTrees = trees

  def getSeeds = seeds

  def log2(input: Int) = {
    (math.log(input) / math.log(2)).toInt
  }

  def getTrainPath(inst: fregata.Vector, treeId: Int) = {
    var path = 0l
    var node = 1l

    var i = 0
    while (i < depth - 1) {
      var selectedFeature = 0
      trees(treeId) match {
        case null =>
          selectedFeature = hasher.getHash(node + seeds(treeId)) % numFeatures
          trees(treeId) = MHashMap(node -> selectedFeature)
        case tree =>
          tree.get(node) match {
            case Some(feature) =>
              selectedFeature = feature
            case _ =>
              selectedFeature = hasher.getHash(node + seeds(treeId)) % numFeatures
              trees(treeId).update(node, selectedFeature)
          }
      }

      val xi = if (0d != inst(selectedFeature)) 1l else 0l
      path |= xi << (depth - 2 - i).toLong
      node = node * 2 + xi
      i += 1
    }

    (path, (depth - 1).toByte)
  }

  def train(insts: Array[(fregata.Vector, fregata.Num)], f: (Int, Num, (Long, Byte)) => Unit) = {
    val s = MHashSet[Int]()
    val r = new Random(seed)
    while (s.size < numTrees) {
      val seed_ = r.nextInt(Integer.MAX_VALUE)
      if (s.add(seed_))
        seeds += seed_
    }

    val instLength = insts.length
    var i = 0
    while (i < numTrees) {
      var j = 0
      while (j < instLength) {
        val pathDepth = getTrainPath(insts(j)._1, i)
        f(seeds(i), insts(j)._2, pathDepth)
        j += 1
      }
      i += 1
    }
  }

  def prune[T](minLeafCapacity: Int, maxPruneNum :Int, models: MHashMap[(Int, (Long, Byte)), T],
               f:(T, Int,Long, Byte, Int, MHashMap[(Int, (Long, Byte)),T],
                 ArrayBuffer[(Int, (Long, Byte))])=> Boolean) = {
    var bPruneNeeded = true
    var i = 0
    while (i < maxPruneNum && bPruneNeeded) {
      bPruneNeeded = false
      val abRemove = ArrayBuffer[(Int, (Long, Byte))]()
      val newModels = MHashMap[(Int, (Long, Byte)), T]()
      models.foreach {
        case ((seed_, pathDepth@(path, depth_)), sth) =>
          f(sth, minLeafCapacity, path, depth_, seed_, newModels, abRemove)
      }
      abRemove.foreach(models.remove)
      models ++= newModels
      i += 1
    }
  }
}

class RDTClassification(numTrees: Int, depth: Int, numFeatures: Int, numClasses: Int = 2, seed: Long = 20170315l)
  extends RDT(numTrees, depth, numFeatures, seed) {

  private var models = MHashMap[(Int, (Long, Byte)), Array[Int]]()

  def setModels(models: MHashMap[(Int, (Long, Byte)), Array[Int]]) = {
    this.models = models
  }

  def getModels = models

  def trainModels(s: Int, y: Num, pathDepth: (Long, Byte)) = {
    models.getOrElse((s, pathDepth), Array.ofDim[Int](numClasses)) match {
      case count =>
        count(y.toInt) += 1
        models.update((s, pathDepth), count)
    }
  }

  def train(insts: Array[(fregata.Vector, fregata.Num)]) = {
    super.train(insts, trainModels)
    new RDTModel(depth, numClasses, seeds.toArray, trees, models)
  }

  def pruneModels[T](count:T, minLeafCapacity: Int, path_ :Long, depth_ : Byte,
                     s:Int, newModels_ : MHashMap[(Int, (Long, Byte)), T],
                     abRemove:ArrayBuffer[(Int, (Long, Byte))]) =
  {
    val count_ = count.asInstanceOf[Array[Int]]
    var bPruneNeeded = false
    val newModels = newModels_.asInstanceOf[MHashMap[(Int, (Long, Byte)), Array[Int]]]
    if (count_.sum < minLeafCapacity) {
      val shortPath = (path_ >> 1, (depth_ - 1).toByte)
      newModels.getOrElse((s, shortPath), Array.ofDim[Int](numClasses)) match {
        case c =>
          (0 until numClasses).foreach { i => count_(i) += c(i) }
          newModels.update((s, shortPath), count_)
          abRemove.append((s, (path_, depth_)))
      }
      bPruneNeeded = true
    }
    bPruneNeeded
  }

  def prune(minLeafCapacity: Int, maxPruneNum: Int = 1):RDTModel = {
    super.prune[Array[Int]](minLeafCapacity, maxPruneNum, models, pruneModels)
    new RDTModel(depth, numClasses, seeds.toArray, trees, models)
  }
}

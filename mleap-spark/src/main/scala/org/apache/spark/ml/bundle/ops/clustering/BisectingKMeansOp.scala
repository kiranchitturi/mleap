package org.apache.spark.ml.bundle.ops.clustering

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import org.apache.spark.ml.bundle.{ParamSpec, SimpleParamSpec, SimpleSparkOp, SparkBundleContext}
import org.apache.spark.ml.clustering.BisectingKMeansModel
import org.apache.spark.mllib.clustering
import org.apache.spark.mllib.clustering.bundle.tree.clustering.{ClusteringTreeNodeUtil, SparkNodeWrapper}

import scala.util.Try

/**
  * Created by hollinwilkins on 12/26/16.
  */
class BisectingKMeansOp extends SimpleSparkOp[BisectingKMeansModel] {
  implicit val nodeWrapper = SparkNodeWrapper

  override val Model: OpModel[SparkBundleContext, BisectingKMeansModel] = new OpModel[SparkBundleContext, BisectingKMeansModel] {
    override val klazz: Class[BisectingKMeansModel] = classOf[BisectingKMeansModel]

    override def opName: String = Bundle.BuiltinOps.clustering.bisecting_k_means

    override def store(model: Model, obj: BisectingKMeansModel)
                      (implicit context: BundleContext[SparkBundleContext]): Model = {
      ClusteringTreeNodeUtil.write(getParentModel(obj))
      model
    }

    override def load(model: Model)
                     (implicit context: BundleContext[SparkBundleContext]): BisectingKMeansModel = {
      val parentModel = ClusteringTreeNodeUtil.read()
      new BisectingKMeansModel("", parentModel)
    }
  }

  override def sparkLoad(uid: String, shape: NodeShape, model: BisectingKMeansModel): BisectingKMeansModel = {
    new BisectingKMeansModel(uid = uid, parentModel = getParentModel(model))
  }

  override def sparkInputs(obj: BisectingKMeansModel): Seq[ParamSpec] = {
    Seq("features" -> obj.featuresCol)
  }

  override def sparkOutputs(obj: BisectingKMeansModel): Seq[SimpleParamSpec] = {
    Seq("prediction" -> obj.predictionCol)
  }

  private def getParentModel(obj: BisectingKMeansModel): clustering.BisectingKMeansModel = {
    // UGLY: have to use reflection to get this private field :(
    val pmField = Try(obj.getClass.getDeclaredField("org$apache$spark$ml$clustering$BisectingKMeansModel$$parentModel"))
    pmField.get.setAccessible(true)
    pmField.get.get(obj).asInstanceOf[clustering.BisectingKMeansModel]
  }
}

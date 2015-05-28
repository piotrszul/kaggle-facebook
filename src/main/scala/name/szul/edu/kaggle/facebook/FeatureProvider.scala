package name.szul.edu.kaggle.facebook

import scala.collection.Iterable

trait FeatureProvider {

  def name:String
  def compute:Iterable[Tuple2[String,Long]]
}
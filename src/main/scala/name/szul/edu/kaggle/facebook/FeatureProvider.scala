package name.szul.edu.kaggle.facebook

import scala.collection.Iterable
import scala.reflect.ClassTag

abstract class FeatureProvider[T](implicit val tag:ClassTag[T]) {

  
  def typeName = tag.toString.toLowerCase;
  def name:String
  def compute:Iterable[Tuple2[String,T]]
}
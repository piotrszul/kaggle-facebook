package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext
import scala.reflect.ClassTag
import org.apache.spark.sql.catalyst.expressions.Row

class HiveSQLFeatureProviderWithParam[T](baseName:String, query:String, param:String, version:Int=1)(implicit hc:HiveContext, implicit val stag:ClassTag[T]) extends FeatureProvider[T] {
    
  @Override
  val name = baseName + "_" + param
  
  @Override
  def compute:Iterable[Tuple2[String,T]] = {
    //new 
    val actualQuery = String.format(query,param)
    println("Evaluating: "  + name  + " with: " + actualQuery)
    hc.sql(actualQuery
        ).map({r => println(r); (r(0).toString(),r.getAs(1)) }).collect().toList
  }

}

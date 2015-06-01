package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext
import scala.reflect.ClassTag
import org.apache.spark.sql.catalyst.expressions.Row

class HiveSQLDistSummaryWithParam[T](baseName:String, query:String, param:String, version:Int=1)(implicit hc:HiveContext, implicit val stag:ClassTag[T]) extends FeatureProvider[T] {
    
  @Override
  val name = baseName + "_" + param
  
  
  lazy val result = {
    //new 
    
    val query = "SELECT key, avg(cnt), max(cnt), min(cnt)  FROM(%s) GROUP BY key";
    val actualQuery = String.format(query,param)
    println("Evaluating: "  + name  + " with: " + actualQuery)
    hc.sql(actualQuery
        ).map(_.toArray).collect().toList
  }
  
  
  
  @Override
  def compute:Iterable[Tuple2[String,T]] = {
    result.map(a => (a(0).toString, a(1).asInstanceOf[T]))    
  }

}

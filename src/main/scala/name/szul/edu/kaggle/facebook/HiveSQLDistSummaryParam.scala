package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext
import scala.reflect.ClassTag
import org.apache.spark.sql.catalyst.expressions.Row

case class HiveSQLDistSummaryWithParam[T](baseName:String, query:String, param:String, version:Int=1)(implicit hc:HiveContext, implicit val stag:ClassTag[T]) {
    
  val pname = baseName + "_" + param
  
  val stats = List("avg", "max", "min", "dev", "median", "pc90")
  
  lazy val result = {
    val actualQuery = String.format(query,param)
    val summaryQuery = String.format("SELECT key, avg(cnt) as avg, max(cnt) as max, min(cnt) as min, stddev_samp(cnt) as dev, percentile(cnt,0.5) as median, percentile(cnt,0.9) as pc90 FROM(%s) as tmp GROUP BY key", actualQuery);
    println("Evaluating: "  + pname  + " with: " + summaryQuery)
    hc.sql(summaryQuery
        ).map(_.toArray).collect().toList
  }
  
  
  case class  SummaryStat[T](index:Int, statName:String, version:Int=1)(implicit val stag:ClassTag[T]) extends FeatureProvider[T] {

    @Override 
    val name = pname + "_" + stats(index)
    
    @Override
    def compute:Iterable[Tuple2[String,T]] = {
      result.map(a => (a(0).toString, a(index + 1).asInstanceOf[T]))    
    }
    
  }
  
  def expand = stats.zipWithIndex.map{case (n,i) => SummaryStat[T](i,n)(stag) }.toList

}

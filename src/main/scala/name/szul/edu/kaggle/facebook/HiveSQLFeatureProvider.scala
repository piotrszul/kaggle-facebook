package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext

case class HiveSQLFeatureProvider(_name:String, query:String, version:Int = 1)(implicit hc:HiveContext) extends FeatureProvider[Long] {
  
  @Override
  val name = _name;  
  
  @Override
  def compute:Iterable[Tuple2[String,Long]] = {
    hc.sql(query).map({r => println(r); (r(0).toString(), r.getLong(1))}).collect().toList
  }

}

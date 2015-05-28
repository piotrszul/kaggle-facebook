package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext

class HiveSQLFeatureProvider(_name:String, query:String)(implicit hc:HiveContext) extends FeatureProvider {
  
  @Override
  val name = _name;  
  
  @Override
  def compute:Iterable[Tuple2[String,Long]] = {
    hc.sql(query).map(r => (r.getString(1), r.getLong(2))).collect().toList
  }

}
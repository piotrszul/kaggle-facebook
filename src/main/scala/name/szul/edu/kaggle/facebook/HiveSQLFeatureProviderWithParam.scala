package name.szul.edu.kaggle.facebook

import org.apache.spark.sql.hive.HiveContext

class HiveSQLFeatureProviderWithParam(baseName:String, query:String, param:String)(implicit hc:HiveContext) extends FeatureProvider {
  
  @Override
  val name = baseName + "_" + param
  
  @Override
  def compute:Iterable[Tuple2[String,Long]] = {
    //new 
    hc.sql(String.format(query,param)
        ).map({r => println(r); (r(0).toString(), r.getLong(1))}).collect().toList
  }

}

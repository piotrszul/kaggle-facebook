package name.szul.edu.kaggle.facebook

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext

object FeatureGenerator {

  def main(args:Array[String]) = {
    

        val ds = new org.postgresql.ds.PGSimpleDataSource();
        ds.setUser("ubuntu");
        ds.setPassword("qwerty");
        ds.setDatabaseName("fbk");
        ds.setServerName("152.83.253.92");
    
        println("Hello World")  
        val store = new JDBCStorage(ds)
 
        val sc: SparkContext = new SparkContext(new SparkConf().setAppName("Generator"));
        implicit val hc:HiveContext = new HiveContext(sc)
        store.save(new HiveSQLFeatureProvider("total_bids","SELECT bidder_id,COUNT(*) FROM bid_out GROUP BY bidder_id"))
  }
  
  
  
}

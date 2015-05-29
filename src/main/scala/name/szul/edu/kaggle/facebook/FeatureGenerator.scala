package name.szul.edu.kaggle.facebook

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext

object FeatureGenerator {

  val dims = List("country","device","auction")
  
  def main(args:Array[String]) = {
    

        val ds = new org.postgresql.ds.PGSimpleDataSource();
        ds.setUser("ubuntu");
        ds.setPassword("qwerty");
        ds.setDatabaseName("fbk");
        ds.setServerName("152.83.253.92");
    
        println("Hello World")  
        val store = new JDBCStorage(ds)
        
       // println(store.buildJoing(List((1L,"count"),(2L,"dev"),(3L,"xxx"))))

        val sc: SparkContext = new SparkContext(new SparkConf().setAppName("Generator"));
        implicit val hc:HiveContext = new HiveContext(sc)        
        
        val features:List[Iterable[FeatureProvider[_]]] = List(
            dims.map(new HiveSQLFeatureProviderWithParam[Long]("total",
                "SELECT bidder_id,COUNT(*) FROM (SELECT DISTINCT bidder_id,%s FROM bid_out) as dist GROUP BY bidder_id",_)),
            dims.map(new HiveSQLFeatureProviderWithParam[Double]("per_auction_avg",
                "SELECT bidder_id,AVG(cnt) FROM (SELECT bidder_id,auction,COUNT(DISTINCT %s) AS cnt FROM bid_out GROUP BY bidder_id,auction) as tmp  GROUP BY bidder_id",_)),
            Some(new HiveSQLFeatureProviderWithParam[Double]("per_auction_avg_bid",
                "SELECT bidder_id,AVG(cnt) FROM (SELECT bidder_id,auction,COUNT(*) AS cnt FROM bid_out GROUP BY bidder_id,auction) as tmp  GROUP BY bidder_id","")),
            Some(new HiveSQLFeatureProvider("total_bid",
                "SELECT bidder_id,COUNT(*) FROM bid_out GROUP BY bidder_id"))
        )
        features.flatten.foreach {store.save(_)}
        
  }
  
  
  
}

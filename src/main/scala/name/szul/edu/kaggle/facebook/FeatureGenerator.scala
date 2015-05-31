package name.szul.edu.kaggle.facebook

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext

object FeatureGenerator {

  val dims = List("country","device","auction", "merchandise", "ip", "url")
  
  /**
   *  / 1000,000,000
   *  - 9,631,917
   *  0
      13,642
      63,663
      77,305
      127,326
      140,968
   */
  
  val dayUnits = 63663L
  val sessionUnits = 13642L
  
  def retime(time:Long):Long = {
    val scaledTime = time/1000000000L - 9631917L
    val day = scaledTime/dayUnits
    val timeInDay = scaledTime/dayUnits
    day * sessionUnits + timeInDay
  }
  
  def timebin(time:Long, binsPerSession:Int):Long = {
    retime(time)/(sessionUnits/binsPerSession)
  }
  
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
        hc.registerFunction("TIMEBIN", timebin _)
        
        val features:List[Iterable[FeatureProvider[_]]] = List(
            // time related features
            List("10", "100").map(new HiveSQLFeatureProviderWithParam[Double]("per_bin_avg_bid",
                "SELECT bidder_id,AVG(cnt) FROM"  + 
                " (SELECT bidder_id,TIMEBIN(time, %1$s) as grp,COUNT(*) AS cnt FROM bid_out GROUP BY bidder_id,TIMEBIN(time, %1$s)) as tmp  GROUP BY bidder_id",_)),     
            // different    
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

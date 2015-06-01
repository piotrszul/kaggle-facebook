package name.szul.edu.kaggle.facebook

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext

object Try {

  val dims = List("country","device","auction")
  
  def main(args:Array[String]) = {
    

        val ds = new org.postgresql.ds.PGSimpleDataSource();
        ds.setUser("ubuntu");
        ds.setPassword("qwerty");
        ds.setDatabaseName("fbk");
        ds.setServerName("152.83.253.92");
    
        println("Hello World")  
        
        List(9631916842105260L, 9645558894736840L, 9695580000000000L, 9709222052631570L, 9759243157894730L, 9772885210526310L).map(FeatureGenerator.retime _).foreach(println)
        
        
        val store = new JDBCStorage(ds)
        store.printInfo(new TestFeatureProvider())   
        
        val sc: SparkContext = new SparkContext(new SparkConf().setAppName("Generator").setMaster("local"));
        implicit val hc:HiveContext = new HiveContext(sc)        
        
        val features:List[Iterable[FeatureProvider[_]]] = List(
            
            HiveSQLDistSummaryWithParam[Double]("per_bid_stats", "SELECT DISTINCT bidder_id,%s FROM bid_out) as dist GROUP BY bidder_id", "country").expand,
            
            dims.map(new HiveSQLFeatureProviderWithParam[Long]("total",
                "SELECT bidder_id,COUNT(*) FROM (SELECT DISTINCT bidder_id,%s FROM bid_out) as dist GROUP BY bidder_id",_)),
            Some(new HiveSQLFeatureProvider("total_bids",
                "SELECT bidder_id,COUNT(*) FROM bid_out GROUP BY bidder_id"))
        )
        features.flatten.foreach {store.printInfo(_)}
  }
  
  
  
}

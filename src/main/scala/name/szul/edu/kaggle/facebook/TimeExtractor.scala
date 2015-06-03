package name.szul.edu.kaggle.facebook

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext

object TimeExtractor {

  val dims = List("country","device","auction", "merchandise", "ip", "url")
  val dimsNoAuction = List("country","device","merchandise", "ip", "url")
  
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
    val scaledTime = Math.round(time/1000000000.0) - 9631917L
    val day = scaledTime/dayUnits
    val timeInDay = scaledTime%dayUnits
    day * sessionUnits + timeInDay
  }
  
  def timebin(time:Long, binsPerSession:Int):Long = {
    retime(time)/(sessionUnits/binsPerSession)
  }

  def timeslot(time:Long, binsPerSession:Int):Long = {
    timebin(time,binsPerSession) % binsPerSession
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
        hc.registerFunction("TIMESLOT", timeslot _)
        
       val result = hc.sql(String.format("SELECT bidder_id,outcome,TIMEBIN(time, %1$s) as timebin,COUNT(*) AS cnt FROM bid_out WHERE outcome IS NOT NULL GROUP BY bidder_id,outcome,TIMEBIN(time, %1$s)", "500"))
       result.map(r => (r(0), r(1), r(2))).saveAsTextFile(args(0))
  }
  
  
  
}

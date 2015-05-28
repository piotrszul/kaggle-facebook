package name.szul.edu.kaggle.facebook

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import scala.collection.mutable.MutableList

class JDBCStorage(val ds:DataSource) {

  
  def doWithConnection(f:Connection => Unit) = {
      var conn:Connection = null
      try {
        conn = ds.getConnection()
        f(conn)
      } finally {
        if (conn!=null) {
          conn.close();
        }
      }   
  }
  
  def doWithStmt(conn:Connection)(s:Connection=>PreparedStatement)(f:PreparedStatement=>Unit) =  {
    var stmt:PreparedStatement = null;
    try {
      stmt = s(conn);
      f(stmt)
    } finally {
      if (stmt!=null) {
        stmt.close();
      }
    }
  }

  def queryWithStmt[T](conn:Connection)(s:Connection=>PreparedStatement)(f:ResultSet=>T) =  {
    var stmt:PreparedStatement = null;
    var rs:ResultSet = null;
    try {
      stmt = s(conn);
      rs = stmt.executeQuery();
      f(rs)
    } finally {
      if (rs!=null) {
        rs.close();
      }
      if (stmt!=null) {
        stmt.close();
      }
    }
  }
  
   def queryWithStmtCust[T](conn:Connection)(s:Connection=>PreparedStatement)(c:PreparedStatement=>Unit)(f:ResultSet=>T) =  {
     queryWithStmt(conn)({conn => val stmt = s(conn); c(stmt); stmt})(f)
   }
  
  def doInTransaction(f:Connection=>Unit):(Connection=>Unit) = {
   { conn:Connection =>
       try {
        conn.setAutoCommit(false)
        f(conn)
        conn.commit();
      } finally {
        conn.rollback();
      }     
    }
  }
  
  def selectLong(conn:Connection)(s:Connection=>PreparedStatement):Option[Long] = {
    queryWithStmt(conn)(s) { rs => if (rs.next()) Some(rs.getLong(1)) else None }
  }
  
  def returnLong(rs:ResultSet):Option[Long] = if (rs.next()) Some(rs.getLong(1)) else None
  
  def save(fp: FeatureProvider) = {
    
    println("Hello !!!!!")
    doWithConnection { 
      doInTransaction { conn =>
        
        println("In TX:")
        val id:Long = queryWithStmtCust(conn)(
            _.prepareStatement("SELECT feature_id FROM feature WHERE key=?"))(_.setString(1,fp.name))(returnLong _).orElse {
          queryWithStmtCust(conn)(
            _.prepareStatement("INSERT INTO feature(key) VALUES(?) RETURNING feature_id"))(_.setString(1,fp.name))(returnLong _)
        }.get

        System.out.println("ID: " + id)
        doWithStmt(conn)(_.prepareStatement("DELETE FROM feature_long WHERE feature_id=?")) { stmt =>
           stmt.setLong(1,id)
           stmt.executeUpdate()
        }
        
        
        doWithStmt(conn)(_.prepareStatement("INSERT INTO feature_long(feature_id,bidder_id,value) VALUES(?,?,?)")) {
          stmt =>
            fp.compute.foreach { t =>
              stmt.setLong(1, id)
              stmt.setString(2,t._1)
              stmt.setLong(3,t._2)
              stmt.addBatch()
            }
            stmt.executeBatch()
        }
      }
    }
  }
  
  def buildJoing(features:List[Tuple2[Long,String]]):String  = {
      val select = features.map {case (id,key) => s"${key}.value as ${key}"} mkString(",")
      val join = features.map {case (id,key) => s"JOIN feature_long as ${key} USING(bidder_id)"} mkString(" ")    
      val where = features.map {case (id,key) => s"${key}.feature_id = ${id}"} mkString(" AND ")
      s"SELECT bidder.bidder_id, bidder.outcome, ${select} FROM bidder ${join} WHERE ${where}"    
  }
  
  def createView() = {
    doWithConnection {conn =>
      doWithStmt(conn)(_.prepareStatement("DROP VIEW  IF EXISTS data "))(_.execute())
      val features = queryWithStmt(conn)(_.prepareStatement("SELECT feature_id, key FROM feature")) { rs =>
        val result = new MutableList[Tuple2[Long,String]]();
        while(rs.next()) {
          result+=((rs.getLong(1), rs.getString(2)))
        }
        result.toList
      }
      val constructViewQuery = "CREATE MATERIALIZED VIEW data AS " + buildJoing(features);
      println("View query: " + constructViewQuery)
      doWithStmt(conn)(_.prepareStatement(constructViewQuery))(_.execute());
      // construct this query
    }
  }
  
}
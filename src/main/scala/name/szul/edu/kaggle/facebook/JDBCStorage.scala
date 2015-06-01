package name.szul.edu.kaggle.facebook

import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import scala.collection.mutable.MutableList
import scala.reflect.ClassTag

case class Feature(val id:Long, val key:String, val typeName:String, val hash:Long)

object Feature {
  def fromNextRs(rs:ResultSet):Option[Feature] = {
    if (rs.next()) Some(fromRs(rs)) else None
  }

  def fromRs(rs:ResultSet):Feature = {
    Feature(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getLong(4))
  }
}


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

  def printInfo(fp: FeatureProvider[_]) = {
    
    println("name: " + fp.name)
    println("type: " + fp.typeName)
    
  }
  
  def save[T](fp: FeatureProvider[T])(implicit tag:ClassTag[T]) = {
    
    println("Hello !!!!!")
    doWithConnection { 
      doInTransaction { conn =>
        
        println("In TX:")
        val f:Feature = queryWithStmtCust(conn)(
            _.prepareStatement("SELECT feature_id,key,type,hash FROM feature WHERE key=?"))(_.setString(1,fp.name))(Feature.fromNextRs _).orElse {
          queryWithStmtCust(conn)(
            _.prepareStatement("INSERT INTO feature(key,type,hash) VALUES(?,?,?) RETURNING feature_id,key,type,0"))({rs => rs.setString(1,fp.name); rs.setString(2,fp.typeName)})(Feature.fromNextRs _)
        }.get

        
        println("Processing feature: " + f)
        if (f.hash == fp.hashCode()) {
          println("Skipping evaluation: hash = " + fp.hashCode())
        } else {
          println("Evaluating: hash = " + fp.hashCode())
          doWithStmt(conn)(_.prepareStatement("DELETE FROM feature_" + fp.typeName + " WHERE feature_id=?")) { stmt =>
             stmt.setLong(1,f.id)
             stmt.executeUpdate()
          }
          doWithStmt(conn)(_.prepareStatement("INSERT INTO feature_" + fp.typeName + "(feature_id,bidder_id,value) VALUES(?,?,?)")) {
            stmt =>
              fp.compute.foreach { t =>
                stmt.setLong(1, f.id)
                stmt.setString(2,t._1)
                stmt.setObject(3,t._2)
                stmt.addBatch()
              }
              stmt.executeBatch()
          }
          
          doWithStmt(conn)(_.prepareStatement("UPDATE  feature SET hash=? WHERE feature_id = ?")) { stmt =>
            stmt.setLong(1, f.hashCode())
            stmt.setLong(2, f.id)
            stmt.executeUpdate()
          }
        }
      }
    }
  }
  
  def buildJoing(features:List[Feature]):String  = {
      val select = features.map {f => s"COALESCE(${f.key}.value,0) as ${f.key}"} mkString(",")
      val join = features.map {f => s"LEFT OUTER JOIN feature_${f.typeName} as ${f.key} ON bidder.bidder_id = ${f.key}.bidder_id AND ${f.key}.feature_id = ${f.id}"} mkString(" ")    
      val where = features.map {f => s"${f.key}.feature_id = ${f.id}"} mkString(" AND ")
      s"SELECT bidder.bidder_id, bidder.outcome, ${select} FROM bidder ${join}"    
  }
  
  def createView() = {
    doWithConnection {conn =>
      doWithStmt(conn)(_.prepareStatement("DROP MATERIALIZED VIEW  IF EXISTS data "))(_.execute())
      val features = queryWithStmt(conn)(_.prepareStatement("SELECT feature_id, key, type, 0 FROM feature")) { rs =>
        val result = new MutableList[Feature]();
        while(rs.next()) {
          result+=Feature.fromRs(rs);
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
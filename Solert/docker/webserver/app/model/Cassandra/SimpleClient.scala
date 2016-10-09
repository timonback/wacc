package model.Cassandra

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSetFuture
import com.datastax.driver.core.Session
import scala.collection.JavaConversions._
import play.api.Logger
import com.datastax.driver.core.Metadata


/**
  * Simple cassandra client, following the datastax documentation
  * (http://www.datastax.com/documentation/developer/java-driver/2.0/java-driver/quick_start/qsSimpleClientCreate_t.html).
  */
//https://github.com/magro/play2-scala-cassandra-sample
class SimpleClient(node: String, port: Int) {

  private val cluster = Cluster.builder().addContactPoint(node).withPort(port).build()
  log(cluster.getMetadata())
  val session = cluster.connect()

  private def log(metadata: Metadata): Unit = {
    Logger.info(s"Connected to cluster: ${metadata.getClusterName}")
    for (host <- metadata.getAllHosts()) {
      Logger.info(s"Datatacenter: ${host.getDatacenter()}; Host: ${host.getAddress()}; Rack: ${host.getRack()}")
    }
  }

  def createSchema(): Unit = {
    session.execute("CREATE KEYSPACE IF NOT EXISTS solert WITH replication = {'class':'SimpleStrategy', 'replication_factor':3};")

    //Execute statements to create two new tables, songs and playlists. Add to the createSchema method:
    session.execute(
      """CREATE TABLE IF NOT EXISTS solert.5_00_51_00 (
        time timestamp PRIMARY KEY,
        value int,
        );""")
  }

  def loadData() = {
    session.execute(
      """INSERT INTO solert.5_00_51_00 (time, value)
      VALUES (
          yyyy-mm-dd'T'HH:mm:ssZ,
          20)
          ;""");
  }

  def querySchema() = {
    val results = session.execute("SELECT * FROM solert.5_00_51_00;")
    
    for (row <- results) {
      println(String.format("%-30s\t%-20s", row.getString("time"),
        row.getString("value")));
    }
  }

  def countFrom(table: String): Long = {
    session.execute(s"select count(*) from solert.$table").one.getLong(0)
  }

  def dropSchema() = {
    session.execute("DROP KEYSPACE solert")
  }

  def getRows: ResultSetFuture = {
    val query = QueryBuilder.select().all().from("solert", "songs")
    session.executeAsync(query)
  }

  def close() {
    session.close
    cluster.close
  }

}
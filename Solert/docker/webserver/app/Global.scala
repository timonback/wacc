import model.Cassandra.SimpleClient
import play.api.Application
import play.api.GlobalSettings

object Global extends GlobalSettings {

  private var cassandra: SimpleClient = _

  override def onStart(app: Application) {
    //Pillar.migrate("faker", app)
    //app.configuration.getString("cassandra.node").getOrElse(throw new IllegalArgumentException("No 'cassandra.node' config found.")
    cassandra = new SimpleClient("192.168.11.11", 9042)
  }

  override def onStop(app: Application) {
    cassandra.close
  }

}
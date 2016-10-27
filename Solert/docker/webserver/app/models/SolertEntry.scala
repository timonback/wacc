package models

import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class SolertEntry(
                        location: String,
                        value: Int,
                        datetime: DateTime
                      )

class SolertEntries extends CassandraTable[ConcreteSolertEntry, SolertEntry] {

  object location extends StringColumn(this) with PartitionKey[String]

  object value extends IntColumn(this)

  object datetime extends DateTimeColumn(this) with PrimaryKey[DateTime]

  def fromRow(row: Row): SolertEntry = {
    SolertEntry(
      location(row),
      value(row),
      datetime(row)
    )
  }
}

abstract class ConcreteSolertEntry extends SolertEntries with RootConnector {

  def clear: Future[ResultSet] = create.ifNotExists().future()

  def getById(location: String): Future[Option[SolertEntry]] = {
    select.where(_.location eqs location).one()
  }

  def getFutureEntries(location: String, minutes: Int): Future[Seq[SolertEntry]] = {
    val start = new DateTime().minusMinutes(10)
    val end = new DateTime().plusMinutes(minutes)

    select.where(_.location eqs location).and(_.datetime gte start).and(_.datetime lte end).fetch()
  }

  def store(solertEntry: SolertEntry): Future[ResultSet] = {
    insert
      .value(_.location, solertEntry.location)
      .value(_.value, solertEntry.value)
      .value(_.datetime, solertEntry.datetime)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
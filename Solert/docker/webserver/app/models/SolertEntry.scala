package models

import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class SolertEntry(
                        location: UUID,
                        value: Int,
                        time: DateTime
                      )

class SolertEntries extends CassandraTable[ConcreteSolertEntry, SolertEntry] {

  object location extends UUIDColumn(this) with PartitionKey[UUID]

  object value extends IntColumn(this)

  object time extends DateTimeColumn(this) with PrimaryKey[DateTime]

  def fromRow(row: Row): SolertEntry = {
    SolertEntry(
      location(row),
      value(row),
      time(row)
    )
  }
}

abstract class ConcreteSolertEntry extends SolertEntries with RootConnector {

  def getById(location: UUID): Future[Option[SolertEntry]] = {
    select.where(_.location eqs location).one()
  }

  def getFutureEntries(location: UUID, hours: Int): Future[Seq[SolertEntry]] = {
    val start = new DateTime()
    val end = new DateTime().plusHours(hours)
    //.and(_.time gte start).and(_.time lte end)
    select.where(_.location eqs location).and(_.time gte start).and(_.time lte end).fetch()
  }

  def store(solertEntry: SolertEntry): Future[ResultSet] = {
    insert
      .value(_.location, solertEntry.location)
      .value(_.value, solertEntry.value)
      .value(_.time, solertEntry.time)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future()
  }
}
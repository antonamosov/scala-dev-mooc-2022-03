package module4.phoneBook.dao.repositories

import module4.phoneBook.db
import zio.Has
import module4.phoneBook.dao.entities._
import io.getquill.CompositeNamingStrategy2
import io.getquill.Escape
import io.getquill.Literal
import zio.ZLayer
import zio.ULayer
import zio.Task
import io.getquill.context.ZioJdbc._
import io.getquill.Ord

object PhoneRecordRepository {
  val ctx = db.Ctx
  import ctx._

  type PhoneRecordRepository = Has[Service]

  trait Service{
      def find(phone: String): QIO[Option[PhoneRecord]]
      def list(): QIO[List[PhoneRecord]]
      def insert(phoneRecord: PhoneRecord): QIO[Unit]
      def update(phoneRecord: PhoneRecord): QIO[Unit]
      def delete(id: String): QIO[Unit]
  }

  class Impl extends Service {

    val phoneRecordSchema = quote{
      querySchema[PhoneRecord]("""PhoneRecord""")
    }
    val addressSchema = quote{
      querySchema[Address]("""Address""")
    }

    // SELECT ph."id", ph."phone", ph."fio", ph."addressId" FROM PhoneRecord ph WHERE ph."phone" = ?
    // SELECT ph."id", ph."phone", ph."fio", ph."addressId" FROM PhoneRecord ph WHERE ph."phone" = ? LIMIT 1
    def find(phone: String): QIO[Option[PhoneRecord]] = ctx.run(
      phoneRecordSchema.filter(ph => ph.phone == lift(phone)).take(1)
    ).map(_.headOption)
    
    // SELECT x."id", x."phone", x."fio", x."addressId" FROM PhoneRecord xb
    def list(): QIO[List[PhoneRecord]] = ctx.run(phoneRecordSchema)
    

    // INSERT INTO PhoneRecord ("id","phone","fio","addressId") VALUES (?, ?, ?, ?)b
    def insert(phoneRecord: PhoneRecord): QIO[Unit] = ctx.run(
      phoneRecordSchema.insert(lift(phoneRecord))
    ).unit
    
    // INSERT INTO PhoneRecord ("id","phone","fio","addressId") VALUES (?, ?, ?, ?)bl
    def update(phoneRecord: PhoneRecord): QIO[Unit] = ctx.run(
      phoneRecordSchema.filter(_.id == lift(phoneRecord.id)).update(lift(phoneRecord))
    ).unit
    

    // DELETE FROM PhoneRecord WHERE "id" = ?
    def delete(id: String): QIO[Unit] = ctx.run(
      phoneRecordSchema.filter(_.id == lift(id)).delete
    ).unit

    def batchInsert(r: List[PhoneRecord]) = ctx.run(
      liftQuery(r).foreach(e => phoneRecordSchema.insert(e))
    ).unit

    // implicit join

    // SELECT phoneRecord."id", phoneRecord."phone", phoneRecord."fio", phoneRecord."addressId", address."id", address."zipCode", address."streetAddress" 
    // FROM PhoneRecord phoneRecord, Address address WHERE phoneRecord."addressId" = address."id"
    def listWithAddress = ctx.run(
      for{
        phoneRecord <- phoneRecordSchema
        address <- addressSchema if(phoneRecord.addressId == address.id)
      } yield (phoneRecord, address)
    )

    // SELECT x4."id", x4."phone", x4."fio", x4."addressId", x5."id", x5."zipCode", x5."streetAddress" FROM PhoneRecord x4 INNER JOIN Address x5 ON x4."addressId" = x5."id"
    // applicative join
    def listWithAddress2 = ctx.run(
      phoneRecordSchema
      .join(addressSchema).on(_.addressId == _.id)
    )

    // flat join
    def listWithAddress3: QIO[List[(String, String)]] = ctx.run(
      for{
        phoneRecord <- phoneRecordSchema
        address <- addressSchema.join(_.id == phoneRecord.addressId)
      } yield (phoneRecord.id, address.streetAddress)
    )

    def listPaged(i: Int, offset: Int) = ctx.run(
      phoneRecordSchema.take(lift(i)).drop(lift(offset))
    )

  }


 

  val live: ULayer[PhoneRecordRepository] = ZLayer.succeed(new Impl)
}

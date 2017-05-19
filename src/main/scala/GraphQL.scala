package api.graphql

import sangria.schema._
import sangria.macros.derive._
import org.mongodb.scala._

import scala.concurrent.Await
import scala.concurrent.duration._
import org.mongodb.scala.bson.{BsonDouble, BsonString}

object GraphQL {
  // Mongo Connection
  val db = MongoClient().getDatabase("scala-api")
  val col = db.getCollection("pictures")

  // Picture Type
  case class Picture(
    width: Int,
    height: Int,
    url: String
  )
  implicit val PictureType =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("A picture"),
      DocumentField("url", "Picture CDN URL"))

  // Repo
  class PictureRepo {
    private val Pictures = List(
      Picture(100, 200, "kittens.com/fluffy"),
      Picture(200, 400, "kittens.com/gruffy"))

    def pictures: List[Picture] = {
      val docs = Await.result(col.find().limit(10).toFuture(), 1.second)
      val pics = docs.map((doc) => {
        Picture(
          doc.get("width") match {
            case None => 0
            case Some(v: BsonDouble) => v.intValue
          },
          doc.get("height") match {
            case None => 0
            case Some(v: BsonDouble) => v.intValue
          },
          doc.get("url") match {
            case None => ""
            case Some(v: BsonString) => v.getValue
          }
        )
      })
      return pics.toList
    }
  }

  // Query Type
  val QueryType = ObjectType("Query", fields[PictureRepo, Unit](
    Field("pictures", ListType(PictureType),
      description = Some("Returns a list of pictures."),
      resolve = (res) => {
        res.ctx.pictures
      })))

  // Schema
  val schema = Schema(QueryType)
}

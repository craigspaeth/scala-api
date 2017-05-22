package api.graphql

import sangria.schema._
import sangria.macros.derive._
import org.mongodb.scala._

import scala.concurrent.Await
import scala.concurrent.duration._
import org.mongodb.scala.Document
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Updates._

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
  def docToPicture(doc: Document): Picture = {
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
  }
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
      val docs = Await.result(col.find().limit(10).toFuture, 1.second)
      val pics = docs.map((doc) => docToPicture(doc))
      return pics.toList
    }

    def savePicture(args: Args): Picture = {
      val cur = col.findOneAndUpdate(BsonDocument(), set("width", 100))
      val doc = Await.result(cur.toFuture, 1.second)
      println(docToPicture(doc))
      return docToPicture(doc)
    }
  }

  // Query Type
  val QueryType = ObjectType("Query", fields[PictureRepo, Unit](
    Field("pictures", ListType(PictureType),
      description = Some("Returns a list of pictures."),
      resolve = (req) => {
        req.ctx.pictures
      })))

  // Mutation Type
  val WidthArg = Argument("width", IntType)
  val HeightArg = Argument("height", IntType)
  val UrlArg = Argument("url", StringType)
  val MutationType = ObjectType("Mutation", fields[PictureRepo, Unit](
    Field("savePicture", PictureType,
      arguments = WidthArg :: HeightArg :: UrlArg :: Nil,
      description = Some("Saves a picture."),
      resolve = (req) => req.ctx.savePicture(req.args)
    )))

  // Schema
  val schema = Schema(QueryType, Some(MutationType))
}

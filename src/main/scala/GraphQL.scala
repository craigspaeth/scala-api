package api.graphql

import sangria.schema._
import sangria.macros._
import sangria.macros.derive._

object GraphQL {

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
      Pictures
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

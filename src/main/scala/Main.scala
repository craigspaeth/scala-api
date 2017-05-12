import sangria.macros._
import sangria.schema._
import sangria.macros.derive._
import scala.concurrent.Future
import spray.json._
import sangria.execution.Executor
import scala.concurrent._
import ExecutionContext.Implicits.global
import sangria.marshalling.sprayJson._

object HelloWorld {
  def main(args: Array[String]): Unit = {

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
    val query =
      graphql"""
        query { 
          pictures {
            width
            height
            url
          }
        }
      """
    val result: Future[JsValue] =
      Executor.execute(schema, query, new PictureRepo)

    println("res...")
    println(result)
  }
}

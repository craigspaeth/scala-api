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
    case class Picture(width: Int, height: Int, url: Option[String])
    implicit val PictureType =
      deriveObjectType[Unit, Picture](
        ObjectTypeDescription("The product picture"),
        DocumentField("url", "Picture CDN URL"))

    // Product Type and Identifiable Interface
    trait Identifiable {
      def id: String
    }
    val IdentifiableType = InterfaceType(
      "Identifiable",
      "Entity that can be identified",

      fields[Unit, Identifiable](
        Field("id", StringType, resolve = _.value.id)))
    case class Product(id: String, name: String, description: String) extends Identifiable {
      def picture(size: Int): Picture =
        Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
    }
    val ProductType =
      deriveObjectType[Unit, Product](
        Interfaces(IdentifiableType),
        IncludeMethods("picture"))

    // Query Type
    class ProductRepo {
      private val Products = List(
        Product("1", "Cheesecake", "Tasty"),
        Product("2", "Health Potion", "+50 HP"))

      def product(id: String): Option[Product] =
        Products find (_.id == id)

      def products: List[Product] = Products
    }
    val Id = Argument("id", StringType)
    val QueryType = ObjectType("Query", fields[ProductRepo, Unit](
      Field("product", OptionType(ProductType),
        description = Some("Returns a product with specific `id`."),
        arguments = Id :: Nil,
        resolve = c ⇒ c.ctx.product(c arg Id)),

      Field("products", ListType(ProductType),
        description = Some("Returns a list of all available products."),
        resolve = _.ctx.products)))

    // Schema
    val schema = Schema(QueryType)
    val query =
      graphql"""
        query MyProduct {
          product(id: "2") {
            name
            description

            picture(size: 500) {
              width, height, url
            }
          }

          products {
            name
          }
        }
      """
    val result: Future[JsValue] =
      Executor.execute(schema, query, new ProductRepo)

    println("Hello, world!")
    println(result)
  }
}

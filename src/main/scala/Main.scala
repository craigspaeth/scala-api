import api.graphql.GraphQL.{schema, PictureRepo}
import sangria.macros._
import scala.concurrent._
import ExecutionContext.Implicits.global
import sangria.marshalling.sprayJson._
import spray.json._
import sangria.execution.Executor

object Main {
  def main(args: Array[String]): Unit = {
    val result: Future[JsValue] =
      Executor.execute(schema, graphql"""
        query { 
          pictures {
            width
            height
            url
          }
        }
      """, new PictureRepo)

    println("res...")
    println(result)
  }
}

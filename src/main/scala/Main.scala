import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import api.graphql.GraphQL.{schema, PictureRepo}
import sangria.macros._
import scala.concurrent._
import sangria.marshalling.sprayJson._
import spray.json._
import sangria.execution.Executor

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      get {
        println("...")
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
        
        onSuccess(result) {
          // TODO: Better pattern matching & proper schema integration
          case _ => complete(result.toString)
        }
      }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 5000)

    println(s"Server online at http://localhost:5000/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
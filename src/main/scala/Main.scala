import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import api.graphql.GraphQL.{schema, PictureRepo}
import scala.concurrent._
import sangria.marshalling.sprayJson._
import spray.json._
import sangria.execution.Executor
import sangria.parser.QueryParser
import scala.util.{Failure, Success}

object WebServer {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      (post & path("graphql")) {
        entity(as[JsValue]) { json =>
          val JsString(query) = json.asJsObject.fields("query")

          val Success(queryAst) = QueryParser.parse(query)
          complete(Executor.execute(schema, queryAst, new PictureRepo))
        }
      } ~
      get {
        getFromResource("graphiql.html")
      }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 5000)

    println(s"Server online at http://localhost:5000/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
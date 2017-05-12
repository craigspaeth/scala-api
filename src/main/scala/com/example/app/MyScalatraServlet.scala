package com.example.app

import org.scalatra._

case class Flower(slug: String, name: String)

object FlowerData {

  var all = List(
      Flower("yellow-tulip", "Yellow Tulip"),
      Flower("red-rose", "Red Rose"),
      Flower("black-rose", "Black Rose"))
}

class MyScalatraServlet extends ScalaapiStack {

  get("/") {
    <h1>Hi</h1>
  }

}

package com.github.trylks.scarab.copper

import com.github.trylks.scarab.Scarab
import org.apache.http.impl.client.HttpClientBuilder

trait CopperScarab extends Scarab {

    case class Response(val head: Seq[(String, String)], val body: String)

    val client = HttpClientBuilder.create().build()

    def get(url: String): Response = Response(Seq(), "") // TODO: pending

    def post(url: String, values: Map[String, String]): Response = Response(Seq(), "") // TODO: pending

    def form(page: String, values: Map[String, String]): Response = Response(Seq(), "") // TODO: pending

}
package com.github.trylks.scarab.copper

import com.github.trylks.scarab.Scarab
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import com.github.trylks.scarab.ScarabCommons._
import org.apache.http.Header
import java.io.Closeable
import scala.io.Source.fromInputStream
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.http.client.methods.HttpRequestBase

class CopperScarab extends Scarab with Closeable {
    val exceptDoc = new Document("http://exception.com/")

    case class Response(val head: Seq[Header], val body: Document)

    private val client = HttpClientBuilder.create().build()

    private def execute(request: HttpRequestBase): Response = {
        using(client.execute(request)) { response =>
            using(response.getEntity().getContent()) { content =>
                val document = Jsoup.parse(fromInputStream(content).mkString)
                Response(response.getAllHeaders(), document)
            }
        }
    }

    def get(url: String): Response = {
        execute(new HttpGet(url))
    }

    def save(url: String, values: Map[String, String] = Map(), path: String) = "good" // TODO: pending  

    def post(url: String, values: Map[String, String]): Response = Response(Seq(), exceptDoc) // TODO: pending

    def form(page: String, values: Map[String, String]): Response = Response(Seq(), exceptDoc) // TODO: pending

    def close() = client.close

}
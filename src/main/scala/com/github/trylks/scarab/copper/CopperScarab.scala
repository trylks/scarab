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
import scala.collection.JavaConverters._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.message.BasicHeader
import com.github.trylks.scarab.implicits._
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.config.RequestConfig
import org.apache.http.message.BasicNameValuePair
import scala.Function.tupled
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.jsoup.nodes.Element

class CopperScarab extends Scarab with Closeable {

    type Response = (Seq[Header], Document)

    val exceptDoc = new Document("http://exception.com/")

    private val headers = Seq(("Accept-Language", "en-US,en;q=0.5"), ("Accept-Encoding", "gzip, deflate"))
    private val client = HttpClientBuilder
        .create()
        .setDefaultCookieStore(new BasicCookieStore())
        .setUserAgent(cs"scarab.copper.userAgent")
        .setDefaultHeaders(headers.map(tupled(new BasicHeader(_, _))).asJavaCollection)
        .build()

    private def execute(request: HttpRequestBase): Response = {
        // 	request.addHeader("", "")
        using(client.execute(request)) { response =>
            using(response.getEntity().getContent()) { content =>
                val document = Jsoup.parse(fromInputStream(content).mkString)
                (response.getAllHeaders(), document)
            }
        }
    }

    def get(url: String): Response = execute(new HttpGet(url))

    def save(url: String, values: Map[String, String] = Map(), path: String) = "good" // TODO: pending  

    def post(url: String, values: Map[String, String]): Response = {
        // TODO: check what to do with fileEntities, start by checking how does http work...
        val encodedValues = new UrlEncodedFormEntity(values.map(tupled(new BasicNameValuePair(_, _))).asJava)
        val request = new HttpPost(url)
        request setEntity encodedValues
        execute(request)
    }

    def form(url: String, values: Map[String, String]): Response = {
        val (head, doc) = get(url)
        val form = doc.getElementsByTag("form").asScala.filter {
            e => !values.keys.exists(e.getElementsByAttributeValue("name", _).isEmpty())
        }(0)
        val fields = form.getElementsByAttribute("value").asScala
        val expandedValues = (fields.map(e => (e.attr("name") -> e.attr("value"))) ++ values).toMap
        post(url, expandedValues)
    }

    def close() = client.close

}
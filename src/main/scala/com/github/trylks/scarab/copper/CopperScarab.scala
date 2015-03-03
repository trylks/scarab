package com.github.trylks.scarab.copper

import com.github.trylks.scarab.Scarab
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import com.github.trylks.scarab.ScarabCommons._
import org.apache.http.Header
import java.io.Closeable
import scala.io.Source._
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
import org.apache.http.client.utils.URIBuilder
import org.apache.http.client.utils.URIUtils
import org.apache.http.StatusLine
import scala.annotation.tailrec

class CopperScarab extends Scarab with Closeable {

    case class Response(status: StatusLine, head: Map[String, String], doc: Document, path: Seq[String])

    val exceptDoc = new Document("http://exception.com/")

    private val headers = Seq(("Accept-Language", "en-US,en;q=0.5"), ("Accept-Encoding", "gzip, deflate"))
    private val client = HttpClientBuilder
        .create()
        .setDefaultCookieStore(new BasicCookieStore())
        .setUserAgent(cs"scarab.copper.userAgent")
        .setDefaultHeaders(headers.map(tupled(new BasicHeader(_, _))).asJavaCollection)
        .build()

    private def singleExecute(request: HttpRequestBase, paths: Seq[String]): Response = {
        // 	request.addHeader("", "") // todo: add referers...
        using(client.execute(request)) { response =>
            using(response.getEntity().getContent()) { content =>
                Response(
                    response.getStatusLine(),
                    response.getAllHeaders().map(h => h.getName() -> h.getValue()).toMap,
                    Jsoup.parse(fromInputStream(content).mkString),
                    paths :+ request.getURI().toString()
                )
            }
        }
    }

    private def isRedirect(r: Response): Boolean = r.head.contains("Location")
    private def getRedirect(r: Response): String = r.head.getOrElse("Location", "/")

    @tailrec
    private def followExecute(request: HttpRequestBase, path: Seq[String] = Seq()): Response = {
        val res = singleExecute(request, path)
        if (isRedirect(res))
            followExecute(new HttpGet(formTargetURL(request.getURI.toString, getRedirect(res))), path :+ request.getURI.toString)
        else
            res
    }

    def download(url: String): Unit = {} // TODO

    def get(url: String): Response = followExecute(new HttpGet(url))

    def save(url: String, values: Map[String, String] = Map(), path: String) = "good" // TODO: pending  

    def post(url: String, values: Map[String, String]): Response = {
        // TODO: check what to do with fileEntities, start by checking how does http work...
        // probably in not one single way, this is something to study further based on the intended use
        val encodedValues = new UrlEncodedFormEntity(values.map(tupled(new BasicNameValuePair(_, _))).asJava)
        val request = new HttpPost(url)
        request setEntity encodedValues
        followExecute(request)
    }

    def form(url: String, values: Map[String, String]): Response = {
        val r = get(url)
        val form = r.doc.getElementsByTag("form").asScala.filter {
            e => !values.keys.exists(e.getElementsByAttributeValue("name", _).isEmpty())
        }(0)
        val fields = form.getElementsByAttribute("value").asScala
        val expandedValues = (fields.map(e => (e.attr("name") -> e.attr("value"))) ++ values).toMap
        post(formTargetURL(url, form.attr("action")), expandedValues)
    }

    private def formTargetURL(url: String, actionURL: String): String =
        new URIBuilder(url).setPath(actionURL).build().toString()

    def close() = client.close

}
package com.github.trylks.scarab.copper

import java.io.Closeable
import java.io.FileOutputStream
import java.io.InputStream

import scala.Function.tupled
import scala.annotation.tailrec
import scala.collection.JavaConverters.asJavaCollectionConverter
import scala.collection.JavaConverters.asJavaIterableConverter
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.io.Source.fromInputStream

import org.apache.commons.io.IOUtils
import org.apache.http.StatusLine
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicNameValuePair
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import com.github.trylks.scarab.Scarab
import com.github.trylks.scarab.ScarabCommons.using
import com.github.trylks.scarab.implicits.StringPrefexes
import scala.language.implicitConversions

class CopperScarab extends Scarab with Closeable {

  implicit def toEncodedEntity(values: Map[String, String]): UrlEncodedFormEntity = new UrlEncodedFormEntity(values.map(tupled(new BasicNameValuePair(_, _))).asJava)
  case class Response(status: StatusLine, head: Map[String, String], content: InputStream, path: Seq[String] = Seq()) {
    def setPaths(paths: Seq[String]) = this.copy(path = paths)
    def doc = Jsoup.parse(fromInputStream(content).mkString)
  }

  val exceptDoc = new Document("http://exception.com/")

  private val headers = Seq(("Accept-Language", "en-US,en;q=0.5"), ("Accept-Encoding", "gzip, deflate"))
  private val client = HttpClientBuilder
    .create()
    .setDefaultCookieStore(new BasicCookieStore())
    .setUserAgent(cs"scarab.copper.userAgent")
    .setDefaultHeaders(headers.map(tupled(new BasicHeader(_, _))).asJavaCollection)
    .build()

  private def singleExecute(request: HttpRequestBase): Response = {
    request.addHeader("Referer", request.getURI.getHost.toString)
    using(client.execute(request)) { response =>
      Response(
        response.getStatusLine,
        response.getAllHeaders.map(h => h.getName -> h.getValue).toMap,
        response.getEntity.getContent)
    }
  }

  private def isRedirect(r: Response): Boolean = r.head.contains("Location")
  private def getRedirect(r: Response): String = r.head.getOrElse("Location", "/")

  @tailrec
  private def followExecute(request: HttpRequestBase, paths: Seq[String] = Seq()): Response = {
    val res = singleExecute(request)
    if (isRedirect(res))
      followExecute(new HttpGet(formTargetURL(request.getURI.toString, getRedirect(res))), paths :+ request.getURI.toString)
    else
      res setPaths paths :+ request.getURI.toString
  }

  def get(url: String): Response = followExecute(new HttpGet(url))

  def download(url: String, path: String, values: Map[String, String] = Map()) = {
    val request = new HttpPost(url)
    request setEntity values
    val res = followExecute(request)
    using(new FileOutputStream(path)) { IOUtils.copy(res.content, _) }
  }

  def post(url: String, values: Map[String, String]): Response = {
    // TODO: check what to do with fileEntities, start by checking how does http work...
    // probably in not one single way, this is something to study further based on the intended use
    val request = new HttpPost(url)
    request setEntity values
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

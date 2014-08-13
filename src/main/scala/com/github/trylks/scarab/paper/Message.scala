package com.github.trylks.scarab.paper

import java.io.File
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import java.util.Date
import java.text.SimpleDateFormat

// I'm not certain about solutions more elegant than this object
// or maybe this is the elegant solution and I should change everything else :-m
object defs {
    val conf = ConfigFactory.load()
    def mailTime(time: Date = new Date()): String = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(time)
}

case class Message(val from: Seq[String] = defs.conf.getStringList("scarab.paper.from").asScala,
                   val to: Seq[String] = defs.conf.getStringList("scarab.paper.to").asScala,
                   val cc: Seq[String] = defs.conf.getStringList("scarab.paper.cc").asScala,
                   val subject: String = defs.conf.getString("scarab.paper.subject"),
                   val body: String = defs.conf.getString("scarab.paper.body"),
                   val htmlbody: String = "",
                   val attachments: Seq[File] = Seq(),
                   val date: String = defs.mailTime(),
                   val imapPointer: javax.mail.Message = null) {
    private def p(elements: Seq[String]): String = elements.mkString(", ")
    private def a(elements: Seq[File]): String = elements.map(_.getAbsolutePath()).mkString(", ")
    override def toString(): String = s"Message:\n\tFrom:\t${p(from)}\n\tTo:\t${p(to)}\n\tCC:\t${p(cc)}\n\tSubject:$subject\n\tBody:\t$body\n\tBody2:\t$htmlbody\n\tAtt:\t${a(attachments)}"
}
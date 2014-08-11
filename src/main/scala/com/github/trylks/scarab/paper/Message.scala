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

case class Message(val from: String = defs.conf.getString("scarab.paper.from"),
                   val to: Seq[String] = defs.conf.getStringList("scarab.paper.to").asScala,
                   val cc: Seq[String] = defs.conf.getStringList("scarab.paper.cc").asScala,
                   val subject: String = defs.conf.getString("scarab.paper.subject"),
                   val body: String = defs.conf.getString("scarab.paper.body"),
                   val attachments: Seq[File] = Seq(),
                   val date: String = defs.mailTime())
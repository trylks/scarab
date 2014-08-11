package com.github.trylks.scarab

import com.typesafe.config._
import java.util.Properties
import scala.language.implicitConversions

trait Scarab {
    val conf = ConfigFactory.load()
}

object implicits {
    val conf = ConfigFactory.load()
    implicit class StringPrefexes(val sc: StringContext) extends AnyVal {
        def cs(args: Any*): String = {
            conf.getString(sc.parts(0))
            //there are probably better ways to do this
        }
    }
    implicit def MapStringString2Properties(that: Map[String, String]): Properties = {
        val res = new Properties()
        for (p <- that)
            res.setProperty(p._1, p._2)
        res
    }
}

/**
 *
 * ToDo: this is a horrible place to write this down, and I don't care.
 *
 * IMAP, IMAP FTW.
 * Integration of paper and stone for the future.
 * Copper is lagging behind. squery? squery!
 *
 *
 */

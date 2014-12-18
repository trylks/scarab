package com.github.trylks.scarab.javaverbs

import java.io.InputStream
import com.github.trylks.scarab.javaverbs.Temporary._
import com.github.trylks.scarab.ScarabCommons._
import java.io.InputStreamReader
import java.io.BufferedReader

object IOOps {

    def asVector(stream: InputStream): Vector[String] = {
        using(new InputStreamReader(stream)) { is =>
            using(new BufferedReader(is)) { br =>
                Iterator.iterate(br.readLine())(x => br.readLine()).takeWhile(_ != null).toVector
            }
        }
    }

    def attempt[A, B](base: A)(block: A => B): Option[B] = {
        val res = block(base)
        if (res == null)
            None
        else
            Some(res)
    }

}
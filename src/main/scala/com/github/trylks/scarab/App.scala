package com.github.trylks.scarab

import com.github.trylks.scarab.paper.PaperScarab
import com.github.trylks.scarab.paper.Message
import java.io.File

/**
 * @author trylks
 */
object App {

    def foo(x: Array[String]) = x.foldLeft("")((a, b) => a + b)

    def main(args: Array[String]) {
        println("Hello World!")
        //println(Message(attachments = Seq(new File("../../../cake.png"))))
        //PaperScarab.send(Message(attachments = Seq(new File("../../../cake.png"))))
        //PaperScarab.send(Message(date = ts.mailAgoBy(days = 3)))
        for (m <- PaperScarab.receiveNew())
            println(m)
        println("Good bye World...")
    }

}

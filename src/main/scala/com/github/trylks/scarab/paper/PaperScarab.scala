package com.github.trylks.scarab.paper

import com.github.trylks.scarab.Scarab
import com.github.trylks.scarab.implicits._
import org.apache.commons.mail._
import scala.collection.JavaConverters._
import javax.mail.Session
import javax.mail.Folder
import javax.mail.internet.InternetAddress
import javax.mail.search.FlagTerm
import javax.mail.Flags
import scala.util.matching.Regex
import java.io.File
import com.sun.mail.util.BASE64DecoderStream
import java.io.FileOutputStream
import org.apache.commons.io.IOUtils

// this was quite helpful: https://gist.github.com/mariussoutier/3436111
// some day I may use that to improve this
// also this: http://stackoverflow.com/questions/848794/sending-email-in-java-using-apache-commons-email-libs

trait PaperScarab extends Scarab with SandScarab {

    val mailHandlers: Map[Regex, Message => Boolean] = Map(".*".r -> (x => false))

    def send(msg: Message) = {
        val mail: MultiPartEmail = new MultiPartEmail()
        mail setHostName cs"scarab.paper.smtphost"
        mail.setAuthentication(cs"scarab.paper.user", cs"scarab.paper.password")
        mail setSmtpPort conf.getInt("scarab.paper.smtpport")
        mail setStartTLSEnabled conf.getBoolean("scarab.paper.tls")
        mail setSSLOnConnect conf.getBoolean("scarab.paper.ssl")
        mail setFrom msg.from(0)
        mail setHeaders Map("Date" -> msg.date).asJava
        // println(s"chosen date: ${msg.date}")
        mail setSubject msg.subject
        mail setMsg msg.body
        for (receiver <- msg.to)
            mail addTo receiver
        for (receiver <- msg.cc)
            mail addCc receiver
        for (att <- msg.attachments) {
            val attachment = new EmailAttachment()
            attachment setPath att.getAbsolutePath
            attachment setDisposition EmailAttachment.ATTACHMENT
            attachment setName att.getName
            mail attach attachment
        }
        mail.send()
    }

    def note(brief: String, full: String = "This is an automated message.") = {
        send(Message(subject = brief, body = full))
    }

    // helpful pages:
    // https://forums.oracle.com/thread/1590957
    // http://stackoverflow.com/questions/12967591/javamail-unread-message-returns-very-last-messages-rather-than-unread
    // http://docs.oracle.com/javaee/6/api/index.html?javax/mail/Flags.html
    // http://alvinalexander.com/scala/scala-imaps-ssl-email-client-javamail-yahoo-gmail
    def receiveNew() = {
        val session = Session.getDefaultInstance(Map("" -> ""), null)
        val store = session.getStore("imaps")
        //try {
        // use imap.gmail.com for gmail
        store.connect(cs"scarab.paper.imaphost", cs"scarab.paper.user", cs"scarab.paper.password")
        val inbox = store.getFolder("Inbox")
        inbox.open(Folder.READ_WRITE)
        val messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)).map(javax2ME(_))
        println(messages.map(_.toString).mkString("\n\n"))
        inbox.close(true)
        messages
        /*} finally {
            store.close()
        }*/
    }

    def processNew() = {
        receiveNew map processMail forall _
    }

    def processWith(pattern: Regex, function: Message => Boolean, m: Message): Boolean = {
        pattern.findFirstIn(m.subject).nonEmpty && function(m)
    }

    def processMail(m: Message): Boolean = {
        val processed = mailHandlers map (e => processWith(e._1, e._2, m)) exists (x => x)
        if (processed)
            m.imapPointer.setFlag(Flags.Flag.SEEN, true)
        processed
    }

    def convertAddresses(addresses: Array[javax.mail.Address]): Seq[String] = {
        addresses.toSeq.filter(_ != null).map(_.toString)
    }

    def processContent(t: String, fs: Object, name: String): (String, String, Seq[File]) = {
        val files =
            if (name == null)
                Seq()
            else {
                val s = name.lastIndexOf(".")
                val file = File.createTempFile(name.substring(0, s), name.substring(s, name.size))
                saveIn(fs, file, s"t: $t \nc: ${fs.getClass().toString()}\nn: $name")
                Seq(file)
            }
        if (t.equalsIgnoreCase("text/plain"))
            (fs.asInstanceOf[String], "", Seq())
        else if (t.equalsIgnoreCase("text/html"))
            ("", fs.asInstanceOf[String], Seq())
        else if (t.substring(0, 9).equalsIgnoreCase("multipart")) {
            val parts = fs.asInstanceOf[javax.mail.internet.MimeMultipart]
            (0 until parts.getCount()).map(x => parts.getBodyPart(x)).map(p => processContent(p.getContentType(), p.getContent(), p.getFileName())).fold[(String, String, Seq[File])]("", "", Seq())(aggregate)
        } else {
            ("", "", files)
        }
    }

    def saveIn(input: Object, output: File, types: String) = input match {
        case i: BASE64DecoderStream => IOUtils.copy(i, new FileOutputStream(output))
        case _                      => note("unknown mail attachment type", types)
    }

    def aggregate(e1: (String, String, Seq[File]), e2: (String, String, Seq[File])): (String, String, Seq[File]) = {
        (e1._1 + e2._1, e1._2 + e2._2, e1._3 ++ e2._3)
    }

    def javax2ME(m: javax.mail.Message): Message = {
        val (plainText, htmlText, files) = processContent(m.getContentType(), m.getContent(), m.getFileName())
        m.setFlag(Flags.Flag.SEEN, false)
        Message(subject = m.getSubject(),
            from = convertAddresses(m.getReplyTo()),
            to = convertAddresses(m.getAllRecipients()),
            cc = Seq(),
            body = plainText,
            htmlbody = htmlText,
            attachments = files,
            date = m.getHeader("Date")(0),
            imapPointer = m)
    }

}

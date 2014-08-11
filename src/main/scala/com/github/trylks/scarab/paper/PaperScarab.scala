package com.github.trylks.scarab.paper

import com.github.trylks.scarab.Scarab
import com.github.trylks.scarab.implicits._
import org.apache.commons.mail._
import scala.collection.JavaConverters._
import javax.mail.Session
import javax.mail.Folder
import javax.mail.search.FlagTerm
import javax.mail.Flags

// this was quite helpful: https://gist.github.com/mariussoutier/3436111
// some day I may use that to improve this
// also this: http://stackoverflow.com/questions/848794/sending-email-in-java-using-apache-commons-email-libs

trait PaperScarab extends Scarab with SandScarab {

    def send(msg: Message) = {
        val mail: MultiPartEmail = new MultiPartEmail()
        mail setHostName cs"scarab.paper.smtphost"
        mail.setAuthentication(cs"scarab.paper.user", cs"scarab.paper.password")
        mail setSmtpPort conf.getInt("scarab.paper.smtpport")
        mail setStartTLSEnabled conf.getBoolean("scarab.paper.tls")
        mail setSSLOnConnect conf.getBoolean("scarab.paper.ssl")
        mail setFrom msg.from
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
        try {
            // use imap.gmail.com for gmail
            store.connect(cs"scarab.paper.imaphost", cs"scarab.paper.user", cs"scarab.paper.password")
            val inbox = store.getFolder("Inbox")
            inbox.open(Folder.READ_WRITE)
            val messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)).map(javax2ME(_))
            inbox.close(true)
            messages
        } finally {
            store.close()
        }
    }

    def javax2ME(m: javax.mail.Message): Message = {
        Message(subject = m.getSubject(),
            from = m.getReplyTo().toString(),
            to = Nil,
            cc = Nil,
            body = "",
            attachments = Nil,
            date = m.getHeader("Date")(0))

    }
}

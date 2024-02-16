package app.vatov.idserver.mail

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

fun sendMail() {

    val props = Properties()
    props["mail.smtp.auth"] = "true"
    props["mail.smtp.starttls.enable"] = "true"
    props["mail.smtp.host"] = "smtp.gmail.com"
    props["mail.smtp.port"] = 587

//    props["mail.smtp.port"] = 465
//    props["mail.smtp.socketFactory.port"] = "465"
//    props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"

    val auth = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication("who@gmail.com", "pass213")
        }
    }

    val session = Session.getInstance(props, auth)
    val message = MimeMessage(session)

    val sender = InternetAddress("who@gmail.com")
    sender.personal = "Who"
    message.setFrom(sender)
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("who@gmail.com"))
    message.subject = "Spam email"

    message.setText("Please do not spam my email!")

    Transport.send(message)
}
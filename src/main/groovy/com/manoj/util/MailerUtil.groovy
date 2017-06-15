package com.manoj.util

import groovy.util.logging.Slf4j

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Slf4j
@Singleton
class MailerUtil {

    private Properties mailProperties
    ConfigUtil configUtil = ConfigUtil.instance

    Boolean sendMail(String to, String subject, String body) {
        Boolean isSuccessful = true
        try {
            initializeMailProperties()
            Session session = Session.getDefaultInstance(mailProperties);
            MimeMessage message = new MimeMessage(session)
            message.from = new InternetAddress("manoj.mohan@tothenew.com")
            message.addRecipients(Message.RecipientType.TO, new InternetAddress(to))
            configUtil.getCCList().each { String mailId ->
                log.debug("Adding ${mailId} to failure mail list.")
                message.addRecipients(Message.RecipientType.CC, new InternetAddress(mailId))
            }
            message.subject = subject
            message.text = body
            log.debug("Sending Message")
            Transport.send(message)
        } catch (MessagingException e) {
            isSuccessful = false
            e.getLocalizedMessage();
        }
        isSuccessful
    }

    private void initializeMailProperties() {
        log.trace("Mailing Properties now is : ${mailProperties}")
        if (!mailProperties) {
            log.debug("Mailing Properties wern't set .. setting")
            mailProperties = System.getProperties();
            mailProperties.setProperty("mail.smtp.host", configUtil.getMailSMTPHost())
            mailProperties.put("mail.smtp.localhost", "127.0.0.1");
        }
    }
}
package org.ovirt.engine.core.notifier.utils.sender.mail;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * Support email sending by SMTP or SMTP over SSL methods.
 */
public class JavaMailSender {

    private static final Logger log = Logger.getLogger(JavaMailSender.class);
    private Session session = null;
    private InternetAddress from = null;
    private InternetAddress replyTo = null;
    private boolean isBodyHtml = false;
    private boolean isSSL = false;
    private EmailAuthenticator auth;

    /**
     * Creates an instance of {@code javax.mail.Session} which could be used for multiple messages dispatch.
     * @param aMailProps
     *            properties required for creating a mail session
     */
    public JavaMailSender(NotificationProperties aMailProps) {
        Properties mailSessionProps = setCommonProperties(aMailProps);

        mailSessionProps.put("mail.smtp.host", aMailProps.getProperty(NotificationProperties.MAIL_SERVER));
        // enable SSL
        if (aMailProps.getBoolean(NotificationProperties.MAIL_ENABLE_SSL, false)) {
            mailSessionProps.put("mail.transport.protocol", "smtps");
            mailSessionProps.put("mail.smtp.port", aMailProps.getProperty(NotificationProperties.MAIL_PORT_SSL));
            mailSessionProps.put("mail.smtps.socketFactory.port", aMailProps.getProperty(NotificationProperties.MAIL_PORT_SSL));
            mailSessionProps.put("mail.smtps.auth", "true");
            mailSessionProps.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mailSessionProps.put("mail.smtps.socketFactory.fallback", false);

            this.isSSL = true;
        } else {
            mailSessionProps.put("mail.transport.protocol", "smtp");
            mailSessionProps.put("mail.smtp.port", aMailProps.getProperty(NotificationProperties.MAIL_PORT));
        }

        String password = aMailProps.getProperty(NotificationProperties.MAIL_PASSWORD, true);
        if (StringUtils.isNotEmpty(password)) {
            auth = new EmailAuthenticator(aMailProps.getProperty(NotificationProperties.MAIL_USER, true),
                    password);
            this.session = Session.getDefaultInstance(mailSessionProps, auth);
        } else {
            this.session = Session.getInstance(mailSessionProps);
        }

    }

    /**
     * Set common properties for both secured and non-secured mail session
     * @param aMailProps
     *            mail configuration properties
     * @return a session common properties
     */
    private Properties setCommonProperties(NotificationProperties aMailProps) {
        Properties mailSessionProps = new Properties();
        if (log.isTraceEnabled()) {
            mailSessionProps.put("mail.debug", "true");
        }

        String mailHost = aMailProps.getProperty(NotificationProperties.MAIL_SERVER);

        String emailUser = aMailProps.getProperty(NotificationProperties.MAIL_USER, true);
        if (StringUtils.isEmpty(emailUser)) {
            if (aMailProps.getBoolean(NotificationProperties.MAIL_ENABLE_SSL, false) ||
                    StringUtils.isNotEmpty(aMailProps.getProperty(NotificationProperties.MAIL_PASSWORD, true))) {
                throw new IllegalArgumentException(NotificationProperties.MAIL_USER
                        + " must be set when SSL is enabled or when password is set");
            } else {
                log.warn(NotificationProperties.MAIL_USER
                        + " property is empty in notification service configuration file");
            }
        }

        if (StringUtils.isNotEmpty(aMailProps.getProperty(NotificationProperties.MAIL_FROM, true))) {
            try {
                from = new InternetAddress(aMailProps.getProperty(NotificationProperties.MAIL_FROM));
            } catch (AddressException e) {
                log.error(MessageFormat.format("Failed to parse 'from' user {0} provided by property {1}",
                        aMailProps.getProperty(NotificationProperties.MAIL_FROM),
                        NotificationProperties.MAIL_FROM), e);
            }
        } else {
            try {
                if (StringUtils.isNotEmpty(emailUser)) {
                    from = new InternetAddress(emailUser);
                }
            } catch (AddressException e) {
                log.error(MessageFormat.format("Failed to parse 'email user' {0} provided by property {1}",
                        emailUser,
                        NotificationProperties.MAIL_USER), e);
            }
        }

        if (StringUtils.isNotEmpty(aMailProps.getProperty(NotificationProperties.MAIL_REPLY_TO, true))) {
            try {
                replyTo = new InternetAddress(aMailProps.getProperty(NotificationProperties.MAIL_REPLY_TO));
            } catch (AddressException e) {
                log.error(MessageFormat.format("Failed to parse 'replyTo' email {0} provided by property {1}",
                        aMailProps.getProperty(NotificationProperties.MAIL_REPLY_TO),
                        NotificationProperties.MAIL_REPLY_TO), e);
            }
        }

        isBodyHtml = aMailProps.getBoolean(NotificationProperties.HTML_MESSAGE_FORMAT, false);

        return mailSessionProps;
    }

    /**
     * Sends a message to a recipient using pre-configured mail session, either as a plan text message or as a html
     * message body
     * @param recipient
     *            a recipient mail address
     * @param messageSubject
     *            the subject of the message
     * @param messageBody
     *            the body of the message
     * @throws MessagingException
     */
    public void send(String recipient, String messageSubject, String messageBody) throws MessagingException {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(from);
            InternetAddress address = new InternetAddress(recipient);
            msg.setRecipient(Message.RecipientType.TO, address);
            if (replyTo != null) {
                msg.setReplyTo(new Address[] { replyTo });
            }
            msg.setSubject(messageSubject);
            if (isBodyHtml){
                msg.setContent(String.format("<html><head><title>%s</title></head><body><p>%s</body></html>",
                        messageSubject,
                        messageBody), "text/html");
            } else {
                msg.setText(messageBody);
            }
            msg.setSentDate(new Date());
            if (isSSL) {
                Transport transport = session.getTransport("smtps");
                transport.connect();
                transport.sendMessage(msg, new Address[] { address });
            } else {
                Transport.send(msg);
            }
        } catch (MessagingException mex) {
            StringBuilder errorMsg = new StringBuilder("Failed to send message ");
            if (from != null) {
                errorMsg.append(" from " + from.toString());
            }
            if (StringUtils.isNotBlank(recipient)) {
                errorMsg.append(" to " + recipient);
            }
            if (StringUtils.isNotBlank(messageSubject)) {
                errorMsg.append(" with subject " + messageSubject);
            }
            errorMsg.append(" due to to error: " + mex.getMessage());
            log.error(errorMsg.toString(), mex);
            throw mex;
        }
    }

    /**
     * An implementation of the {@link Authenticator}, holds the authentication credentials for a network connection.
     */
    private class EmailAuthenticator extends Authenticator {
        private String userName;
        private String password;
        public EmailAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }
}

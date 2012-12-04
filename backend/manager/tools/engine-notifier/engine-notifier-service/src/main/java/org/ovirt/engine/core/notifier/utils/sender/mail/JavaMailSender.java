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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * Support email sending by SMTP or SMTP over SSL methods.
 */
public class JavaMailSender {

    private static final Log log = LogFactory.getLog(JavaMailSender.class);
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
    public JavaMailSender(Map<String, String> aMailProps) {
        Properties mailSessionProps = setCommonProperties(aMailProps);

        // enable SSL
        if (Boolean.valueOf(aMailProps.get(NotificationProperties.MAIL_ENABLE_SSL))) {
            mailSessionProps.put("mail.transport.protocol", "smtps");
            mailSessionProps.put("mail.smtps.auth", "true");
            mailSessionProps.put("mail.smtps.host", aMailProps.get(NotificationProperties.MAIL_SERVER));
            String portString = aMailProps.get(NotificationProperties.MAIL_PORT);
            if (StringUtils.isNotEmpty(portString)) {
                mailSessionProps.put("mail.smtps.socketFactory.port", Integer.valueOf(portString));
            }
            mailSessionProps.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mailSessionProps.put("mail.smtps.socketFactory.fallback", false);

            this.isSSL = true;
        } else {
            mailSessionProps.put("mail.transport.protocol", "smtp");
            mailSessionProps.put("mail.smtp.host", aMailProps.get(NotificationProperties.MAIL_SERVER));
        }

        String password = aMailProps.get(NotificationProperties.MAIL_PASSWORD);
        boolean isAuthenticated = StringUtils.isNotEmpty(password);
        if (isAuthenticated) {
            auth = new EmailAuthenticator(aMailProps.get(NotificationProperties.MAIL_USER),
                    aMailProps.get(NotificationProperties.MAIL_PASSWORD));
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
    private Properties setCommonProperties(Map<String, String> aMailProps) {
        Properties mailSessionProps = new Properties();
        if (log.isTraceEnabled()) {
            mailSessionProps.put("mail.debug", "true");
        }

        String mailHost = aMailProps.get(NotificationProperties.MAIL_SERVER);
        if (StringUtils.isEmpty(mailHost)) {
            throw new IllegalArgumentException(NotificationProperties.MAIL_SERVER + " must not be null or empty");
        }

        String emailUser = aMailProps.get(NotificationProperties.MAIL_USER);
        if (StringUtils.isEmpty(emailUser)) {
            if (Boolean.valueOf(aMailProps.get(NotificationProperties.MAIL_ENABLE_SSL)) ||
                    StringUtils.isNotEmpty(aMailProps.get(NotificationProperties.MAIL_PASSWORD))) {
                throw new IllegalArgumentException(NotificationProperties.MAIL_USER
                        + " must be set when SSL is enabled or when password is set");
            } else {
                log.warn(NotificationProperties.MAIL_USER
                        + " property is empty in notification service configuration file");
            }
        }

        if (StringUtils.isNotEmpty(aMailProps.get(NotificationProperties.MAIL_FROM))) {
            try {
                from = new InternetAddress(aMailProps.get(NotificationProperties.MAIL_FROM));
            } catch (AddressException e) {
                log.error(MessageFormat.format("Failed to parse 'from' user {0} provided by property {1}",
                        aMailProps.get(NotificationProperties.MAIL_FROM),
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

        if (StringUtils.isNotEmpty(aMailProps.get(NotificationProperties.MAIL_REPLY_TO))) {
            try {
                replyTo = new InternetAddress(aMailProps.get(NotificationProperties.MAIL_REPLY_TO));
            } catch (AddressException e) {
                log.error(MessageFormat.format("Failed to parse 'replyTo' email {0} provided by property {1}",
                        aMailProps.get(NotificationProperties.MAIL_REPLY_TO),
                        NotificationProperties.MAIL_REPLY_TO), e);
            }
        }

        String isBodyHtmlStr = aMailProps.get(NotificationProperties.HTML_MESSAGE_FORMAT);
        if (StringUtils.isNotEmpty(isBodyHtmlStr)) {
            isBodyHtml = Boolean.valueOf(isBodyHtmlStr);
        }
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

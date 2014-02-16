package org.ovirt.engine.core.notifier.transport.smtp;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.businessentities.AuditLogEvent;
import org.ovirt.engine.core.common.businessentities.AuditLogEventSubscriber;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.transport.EventSenderResult;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * The class sends e-mails to event subscribers.
 * In order to define a proper mail client, the following properties should be provided:
 * <li><code>MAIL_SERVER</code> mail server name
 * <li><code>MAIL_PORT</code> mail server port</li><br>
 * The following properties are optional: <br>
 * <li><code>MAIL_USER</code> user name includes a domain (e.g. user@test.com)</li>
 * <li><code>MAIL_PASSWORD</code> user's password</li>
 * <ul>if failed to obtain or uses "localhost" if <code>MAIL_MACHINE_NAME</code> not provided</li>
 * <li><code>MAIL_FROM</code> specifies "from" address in sent message, or uses value of property <code>MAIL_USER</code> if not provided</li>
 * <ul><li>"from" address should include a domain, same as <code>MAIL_USER</code> property
 * <li><code>MAIL_REPLY_TO</code> specifies "replyTo" address in outgoing message
 */
public class Smtp implements Transport {

    private static final String MAIL_SERVER = "MAIL_SERVER";
    private static final String MAIL_PORT = "MAIL_PORT";
    private static final String MAIL_USER = "MAIL_USER";
    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    private static final String MAIL_FROM = "MAIL_FROM";
    private static final String MAIL_REPLY_TO = "MAIL_REPLY_TO";
    private static final String HTML_MESSAGE_FORMAT = "HTML_MESSAGE_FORMAT";
    private static final String MAIL_SMTP_ENCRYPTION = "MAIL_SMTP_ENCRYPTION";
    private static final String MAIL_SMTP_ENCRYPTION_NONE = "none";
    private static final String MAIL_SMTP_ENCRYPTION_SSL = "ssl";
    private static final String MAIL_SMTP_ENCRYPTION_TLS = "tls";
    private static final String GENERIC_VALIDATION_MESSAGE = "Check configuration file, ";

    private static final Logger log = Logger.getLogger(Smtp.class);
    private String hostName;
    private boolean isBodyHtml = false;
    private Session session = null;
    private InternetAddress from = null;
    private InternetAddress replyTo = null;
    private EmailAuthenticator auth;

    public Smtp(NotificationProperties props) {

        Properties mailSessionProps =  new Properties();

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Smtp.log.error("Failed to resolve machine name, using localhost instead.", e);
            hostName = "localhost";
        }

        isBodyHtml = props.getBoolean(HTML_MESSAGE_FORMAT, false);
        from = props.validateEmail(MAIL_FROM);
        replyTo = props.validateEmail(MAIL_REPLY_TO);

        if (log.isTraceEnabled()) {
            mailSessionProps.put("mail.debug", "true");
        }

        mailSessionProps.put("mail.smtp.host", props.getProperty(MAIL_SERVER));
        mailSessionProps.put("mail.smtp.port", props.getProperty(MAIL_PORT));
        // enable SSL
        if (MAIL_SMTP_ENCRYPTION_SSL.equals(
                props.getProperty(MAIL_SMTP_ENCRYPTION, true))) {
            mailSessionProps.put("mail.smtp.auth", "true");
            mailSessionProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mailSessionProps.put("mail.smtp.socketFactory.fallback", false);
            mailSessionProps.put("mail.smtp.socketFactory.port", props.getProperty(MAIL_PORT));
        } else if (MAIL_SMTP_ENCRYPTION_TLS.equals(
                props.getProperty(MAIL_SMTP_ENCRYPTION, true))) {
            mailSessionProps.put("mail.smtp.auth", "true");
            mailSessionProps.put("mail.smtp.starttls.enable", "true");
            mailSessionProps.put("mail.smtp.starttls.required", "true");
        }

        String password = props.getProperty(MAIL_PASSWORD, true);
        if (StringUtils.isNotEmpty(password)) {
            auth = new EmailAuthenticator(props.getProperty(Smtp.MAIL_USER, true),
                    password);
            session = Session.getDefaultInstance(mailSessionProps, auth);
        } else {
            session = Session.getInstance(mailSessionProps);
        }
    }

    public static void validate(NotificationProperties props) {
        // validate mandatory and non empty properties
        props.requireOne(MAIL_SERVER);
        // validate MAIL_PORT
        props.requireAll(MAIL_PORT);
        props.validatePort(MAIL_PORT);

        // validate MAIL_USER value
        String emailUser = props.getProperty(MAIL_USER, true);
        if (StringUtils.isEmpty(emailUser) && StringUtils.isNotEmpty(props.getProperty(MAIL_PASSWORD, true))) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be set when password is set",
                            MAIL_USER));
        }

        if (!(MAIL_SMTP_ENCRYPTION_NONE.equals(props.getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_SSL.equals(props.getProperty(MAIL_SMTP_ENCRYPTION, true))
                || MAIL_SMTP_ENCRYPTION_TLS.equals(props.getProperty(MAIL_SMTP_ENCRYPTION, true)))) {
            throw new IllegalArgumentException(
                    String.format(
                            GENERIC_VALIDATION_MESSAGE + "'%s' value has to be one of: '%s', '%s', '%s'.",
                            MAIL_SMTP_ENCRYPTION,
                            MAIL_SMTP_ENCRYPTION_NONE,
                            MAIL_SMTP_ENCRYPTION_SSL,
                            MAIL_SMTP_ENCRYPTION_TLS
                            ));
        }

        // validate email addresses
        for (String property : new String[] {
                MAIL_USER,
                MAIL_FROM,
                MAIL_REPLY_TO }) {
            props.validateEmail(property);
        }
    }

    public EventSenderResult send(AuditLogEvent event, AuditLogEventSubscriber subscriber) {
        EventSenderResult result = new EventSenderResult();
        EventMessageContent message = new EventMessageContent();
        message.prepareMessage(hostName, event, isBodyHtml);

        String recipient = subscriber.getMethodAddress();

        if (StringUtils.isEmpty(recipient)) {
            log.error("Email recipient is not known, please check user table ( email )" +
                    " or event_subscriber ( method_address )," +
                    " unable to send email for subscriber " + subscriber.getSubscriberId() + "," +
                    " message was " + message.getMessageSubject() + ":" + message.getMessageBody());
            result.setSent(false);
            return result;
        }
        log.info(String.format("Send email to [%s]%n subject:%n [%s]",
                recipient,
                message.getMessageSubject()));
        if (log.isDebugEnabled()) {
            log.debug(String.format("body:%n [%s]",
                    message.getMessageBody()));
        }

        boolean shouldRetry = false;
        try {
            sendMail(recipient, message.getMessageSubject(), message.getMessageBody());
        } catch (MessagingException ex) {
            result.setReason(ex.getMessage());
            shouldRetry = true;
        }

        // Attempt additional 3 retries in case of failure
        for (int i = 0; i < 3 && shouldRetry; ++i) {
            shouldRetry = false;
            try {
                // hold the next send attempt for 30 seconds in case of a busy mail server
                Thread.sleep(30000);
                sendMail(recipient, message.getMessageSubject(), message.getMessageBody());
            } catch (MessagingException ex) {
                result.setReason(ex.getMessage());
                shouldRetry = true;
            } catch (InterruptedException e) {
                log.error("Failed to suspend thread for 30 seconds while trying to resend a mail message.", e);
            }
        }
        if (shouldRetry) {
            result.setSent(false);
        } else {
            result.setSent(true);
        }
        return result;
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
    private void sendMail(String recipient, String messageSubject, String messageBody) throws MessagingException {
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
            javax.mail.Transport.send(msg);
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

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }
}


package org.ovirt.engine.core.notifier.transport.smtp;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.notifier.dao.DispatchResult;
import org.ovirt.engine.core.notifier.filter.AuditLogEvent;
import org.ovirt.engine.core.notifier.transport.Transport;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class sends e-mails to event subscribers.
 * In order to define a proper mail client, the following properties should be provided:
 * <ul>
 * <li><code>MAIL_SERVER</code> mail server name
 * <li><code>MAIL_PORT</code> mail server port</li>
 * </ul>
 *
 * The following properties are optional:
 * <ul>
 * <li><code>MAIL_USER</code> user name includes a domain (e.g. user@test.com)</li>
 * <li><code>MAIL_PASSWORD</code> user's password</li>
 * <li>if failed to obtain or uses "localhost" if <code>MAIL_MACHINE_NAME</code> not provided</li>
 * <li><code>MAIL_FROM</code> specifies "from" address in sent message, or uses value of property <code>MAIL_USER</code> if not provided</li>
 * <li>"from" address should include a domain, same as <code>MAIL_USER</code> property</li>
 * <li><code>MAIL_REPLY_TO</code> specifies "replyTo" address in outgoing message</li>
 * </ul>
 */
public class Smtp extends Transport {

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
    private static final String MAIL_SEND_INTERVAL = "MAIL_SEND_INTERVAL";
    private static final String MAIL_RETRIES = "MAIL_RETRIES";

    private static final Logger log = LoggerFactory.getLogger(Smtp.class);
    private int retries;
    private int sendIntervals;
    private int lastSendInterval = 0;
    private final Queue<DispatchAttempt> sendQueue = new LinkedBlockingQueue<>();
    private String hostName;
    private boolean isBodyHtml = false;
    private Session session = null;
    private InternetAddress from = null;
    private InternetAddress replyTo = null;
    private boolean active = false;

    public Smtp(NotificationProperties props) {
        if (!StringUtils.isEmpty(props.getProperty(MAIL_SERVER, true))) {
            active = true;
            init(props);
        }
    }

    private void init(NotificationProperties props) {
        Properties mailSessionProps =  new Properties();

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Smtp.log.error("Failed to resolve machine name, using localhost instead.", e);
            hostName = "localhost";
        }

        retries = props.validateNonNegetive(MAIL_RETRIES);
        sendIntervals = props.validateNonNegetive(MAIL_SEND_INTERVAL);
        isBodyHtml = props.getBoolean(HTML_MESSAGE_FORMAT, false);
        from = props.validateEmail(MAIL_FROM);
        replyTo = props.validateEmail(MAIL_REPLY_TO);

        if (log.isTraceEnabled()) {
            mailSessionProps.put("mail.debug", "true");
        }

        mailSessionProps.put("mail.smtp.host", props.getProperty(MAIL_SERVER));
        mailSessionProps.put("mail.smtp.port", props.validatePort(MAIL_PORT));

        String encryption = props.getProperty(MAIL_SMTP_ENCRYPTION);
        if (MAIL_SMTP_ENCRYPTION_NONE.equals(encryption)) {
            // Do nothing
        } else if (MAIL_SMTP_ENCRYPTION_SSL.equals(encryption)) {
            mailSessionProps.put("mail.smtp.auth", "true");
            mailSessionProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mailSessionProps.put("mail.smtp.socketFactory.fallback", false);
            mailSessionProps.put("mail.smtp.socketFactory.port", props.validatePort(MAIL_PORT));
        } else if (MAIL_SMTP_ENCRYPTION_TLS.equals(encryption)) {
            mailSessionProps.put("mail.smtp.auth", "true");
            mailSessionProps.put("mail.smtp.starttls.enable", "true");
            mailSessionProps.put("mail.smtp.starttls.required", "true");
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "Illegal encryption method for %s",
                    MAIL_SMTP_ENCRYPTION));
        }

        String emailUser = props.getProperty(MAIL_USER, true);
        String emailPassword = props.getProperty(MAIL_PASSWORD, true);
        if (StringUtils.isEmpty(emailUser) && StringUtils.isNotEmpty(emailPassword)) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' must be set when password is set",
                            MAIL_USER));
        }
        if (StringUtils.isNotEmpty(emailPassword)) {
            session = Session.getDefaultInstance(mailSessionProps,
                new EmailAuthenticator(emailUser, emailPassword));
        } else {
            session = Session.getInstance(mailSessionProps);
        }
    }

    @Override
    public String getName() {
        return "smtp";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void dispatchEvent(AuditLogEvent event, String address) {
        if (StringUtils.isEmpty(address)) {
            log.error("Address is empty, cannot distribute message. {}", event.getName());
        } else {
            sendQueue.add(new DispatchAttempt(event, address));
        }
    }

    @Override
    public void idle() {
        if (lastSendInterval++ >= sendIntervals) {
            lastSendInterval = 0;

            Iterator<DispatchAttempt> iterator = sendQueue.iterator();
            while (iterator.hasNext()) {
                DispatchAttempt attempt = iterator.next();
                try {
                    EventMessageContent message = new EventMessageContent();
                    message.prepareMessage(hostName, attempt.event, isBodyHtml);

                    log.info("Sending e-mail subject='{}' to='{}'",
                            message.getMessageSubject(),
                            attempt.address);
                    log.debug("Send e-mail body='{}'", message.getMessageBody());
                    sendMail(attempt.address, message.getMessageSubject(), message.getMessageBody());
                    log.info(
                        "E-mail subject='{}' to='{}' sent successfully",
                        message.getMessageSubject(),
                        attempt.address
                    );
                    notifyObservers(DispatchResult.success(attempt.event, attempt.address, EventNotificationMethod.SMTP));
                    iterator.remove();
                } catch (Exception ex) {
                    attempt.retries++;
                    if (attempt.retries >= retries) {
                        notifyObservers(DispatchResult.failure(attempt.event,
                                attempt.address,
                                EventNotificationMethod.SMTP,
                                ex.getMessage()));
                        iterator.remove();
                    }
                }
            }
        }
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
                errorMsg.append(" from ").append(from.toString());
            }
            if (StringUtils.isNotBlank(recipient)) {
                errorMsg.append(" to ").append(recipient);
            }
            if (StringUtils.isNotBlank(messageSubject)) {
                errorMsg.append(" with subject ").append(messageSubject);
            }
            errorMsg.append(" due to to error: ").append(mex.getMessage());
            log.error(errorMsg.toString(), mex);
            throw mex;
        }
    }

    /**
     * An implementation of the {@link Authenticator}, holds the authentication credentials for a network connection.
     */
    private static class EmailAuthenticator extends Authenticator {
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

    private static class DispatchAttempt {
         public final AuditLogEvent event;
         public final String address;
         public int retries = 0;
         private DispatchAttempt(AuditLogEvent event, String address) {
             this.event = event;
             this.address = address;
         }
     }
}


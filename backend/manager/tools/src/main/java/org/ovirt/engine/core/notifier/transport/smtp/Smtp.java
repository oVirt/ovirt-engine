package org.ovirt.engine.core.notifier.transport.smtp;


import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.mail.MessagingException;

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

    public static final String MAIL_SERVER = "MAIL_SERVER";
    public static final String MAIL_PORT = "MAIL_PORT";
    public static final String MAIL_USER = "MAIL_USER";
    public static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    public static final String MAIL_FROM = "MAIL_FROM";
    public static final String MAIL_REPLY_TO = "MAIL_REPLY_TO";
    public static final String HTML_MESSAGE_FORMAT = "HTML_MESSAGE_FORMAT";
    public static final String MAIL_SMTP_ENCRYPTION = "MAIL_SMTP_ENCRYPTION";
    public static final String MAIL_SMTP_ENCRYPTION_NONE = "none";
    public static final String MAIL_SMTP_ENCRYPTION_SSL = "ssl";
    public static final String MAIL_SMTP_ENCRYPTION_TLS = "tls";
    private static final String GENERIC_VALIDATION_MESSAGE = "Check configuration file, ";

    private static final Logger log = Logger.getLogger(Smtp.class);
    private JavaMailSender mailSender;
    private String hostName;
    private boolean isBodyHtml = false;

    public Smtp(NotificationProperties props) {
        mailSender = new JavaMailSender(props);
        String isBodyHtmlStr = props.getProperty(HTML_MESSAGE_FORMAT);
        if (StringUtils.isNotEmpty(isBodyHtmlStr)) {
            isBodyHtml = Boolean.valueOf(isBodyHtmlStr);
        }

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            Smtp.log.error("Failed to resolve machine name, using localhost instead.", e);
            hostName = "localhost";
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
            mailSender.send(recipient, message.getMessageSubject(), message.getMessageBody());
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
                mailSender.send(recipient, message.getMessageSubject(), message.getMessageBody());
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
}


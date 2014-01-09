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

    private static final Logger log = Logger.getLogger(Smtp.class);
    private JavaMailSender mailSender;
    private String hostName;
    private boolean isBodyHtml = false;

    public Smtp(NotificationProperties props) {
        mailSender = new JavaMailSender(props);
        String isBodyHtmlStr = props.getProperty(NotificationProperties.HTML_MESSAGE_FORMAT);
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


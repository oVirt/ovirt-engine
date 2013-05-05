package org.ovirt.engine.core.notifier.utils.sender.mail;


import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.common.businessentities.EventAuditLogSubscriber;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.sender.EventSender;
import org.ovirt.engine.core.notifier.utils.sender.EventSenderResult;

/**
 * The class designed to send e-mails to subscriptions for events.<br>
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
public class EventSenderMailImpl implements EventSender {

    private static final Logger log = Logger.getLogger(EventSenderMailImpl.class);
    private JavaMailSender mailSender;
    private String hostName;
    private boolean isBodyHtml = false;

    public EventSenderMailImpl(NotificationProperties mailProp) {
        mailSender = new JavaMailSender(mailProp);
        String isBodyHtmlStr = mailProp.getProperty(NotificationProperties.HTML_MESSAGE_FORMAT);
        if (StringUtils.isNotEmpty(isBodyHtmlStr)) {
            isBodyHtml = Boolean.valueOf(isBodyHtmlStr);
        }

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            EventSenderMailImpl.log.error("Failed to resolve machine name, using localhost instead.", e);
            hostName = "localhost";
        }
    }

    /**
     * {@link #EventSender}
     */
    public EventSenderResult send(EventAuditLogSubscriber eventData, String methodAddress) {
        EventSenderResult result = new EventSenderResult();
        EventMessageContent message = new EventMessageContent();
        message.prepareMessage(hostName, eventData, isBodyHtml);

        String recipient = eventData.getmethod_address();
        if (StringUtils.isEmpty(recipient)) {
            recipient = methodAddress;
        }

        if ( StringUtils.isEmpty(recipient) ) {
            log.error("Email recipient is not known, please check user table ( email ) or event_subscriber ( method_address ), unable to send email for subscriber " + eventData.getsubscriber_id() + ", message was " + message.getMessageSubject() + ":" + message.getMessageBody());
            result.setSent(false);
            return result;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Send email to [%s]\n subject:\n [%s]\n body:\n [%s]",
                    recipient,
                    message.getMessageSubject(),
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
        for (int i=0 ; i < 3 && shouldRetry ; ++i){
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


package org.ovirt.engine.core.notifier.transport.smtp;

import java.util.Locale;

import org.ovirt.engine.core.notifier.filter.AuditLogEvent;

/**
 * Creates a simple message subject and body using helper class {@linkplain MessageHelper} or
 * {@linkplain LocalizedMessageHelper} to determine the structure of the message subject and body
 */
public class EventMessageContent {
    private String subject;
    private String body;

    public EventMessageContent() {
    }

    private void prepareMessageSubject(String hostName,
                                       AuditLogEvent event,
                                       Locale locale) {
        subject = (locale == null) ? MessageHelper.prepareMessageSubject(event.getType(), hostName, event.getMessage()) :
                LocalizedMessageHelper.prepareMessageSubject(event.getType(), hostName, event.getMessage(), locale);
    }

    public EventMessageContent(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    private void prepareMessageBody(AuditLogEvent event,
                                    boolean isBodyHtml,
                                    Locale locale) {
        MessageBody messageBody = new MessageBody();
        messageBody.setUserInfo(event.getUserName());
        messageBody.setVmInfo(event.getVmName());
        messageBody.setHostInfo(event.getVdsName());
        messageBody.setTemplateInfo(event.getVmTemplateName());
        messageBody.setDatacenterInfo(event.getStoragePoolName());
        messageBody.setStorageDomainInfo(event.getStorageDomainName());
        messageBody.setLogTime(event.getLogTime());
        messageBody.setSeverity(event.getSeverity());
        messageBody.setMessage(event.getMessage());

        if (isBodyHtml) {
            this.body = (locale == null) ? MessageHelper.prepareHTMLMessageBody(messageBody) :
                    LocalizedMessageHelper.prepareHTMLMessageBody(messageBody, locale);
        } else {
            this.body = (locale == null) ? MessageHelper.prepareMessageBody(messageBody) :
                    LocalizedMessageHelper.prepareMessageBody(messageBody, locale);
        }
    }

    /**
     * returns a readable format of message body
     *
     * @return a readable format of message body
     */
    public String getMessageBody() {
        return body;
    }

    /**
     * returns a readable format of message subject
     *
     * @return a readable format of message subject
     */
    public String getMessageSubject() {
        return subject;
    }

    /**
     * Produces a readable message subject and body based on provided parameters<br>
     * The format of the subject and body are defined by {@linkplain MessageHelper}
     *
     * @param hostName   the host name on which the subject will refer to
     * @param event      associated event which the message will be created by
     * @param isBodyHtml defines the format of message body
     * @param locale     locale for the message content
     */
    public void prepareMessage(String hostName, AuditLogEvent event,
                               boolean isBodyHtml, Locale locale) {
        prepareMessageSubject(hostName, event, locale);
        prepareMessageBody(event, isBodyHtml, locale);
    }

}

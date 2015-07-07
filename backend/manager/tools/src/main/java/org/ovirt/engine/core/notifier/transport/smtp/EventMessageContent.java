package org.ovirt.engine.core.notifier.transport.smtp;

import java.util.Date;

import org.ovirt.engine.core.notifier.filter.AuditLogEvent;

/**
 * Creates a simple message subject and body using helper class {@linkplain MessageHelper} to determine <br>
 * the structure of the message subject and body
 */
public class EventMessageContent {
    private String subject;
    private String body;

    private void prepareMessageSubject(String hostName,
                                       AuditLogEvent event) {
        subject = MessageHelper.prepareMessageSubject(event.getType(), hostName, event.getMessage());
    }

    private void prepareMessageBody(AuditLogEvent event,
                                    boolean isBodyHtml) {
        MessageBody messageBody = new MessageBody();
        messageBody.setUserInfo(event.getUserName());
        messageBody.setVmInfo(event.getVmName());
        messageBody.setHostInfo(event.getVdsName());
        messageBody.setTemplateInfo(event.getVmTemplateName());
        messageBody.setDatacenterInfo(event.getStoragePoolName());
        messageBody.setStorageDomainInfo(event.getStorageDomainName());
        final Date logTime = event.getLogTime();
        messageBody.setLogTime(logTime != null ? logTime.toString() : "");
        messageBody.setSeverity(String.valueOf(event.getSeverity()));
        messageBody.setMessage(event.getMessage());

        if (isBodyHtml) {
            this.body = MessageHelper.prepareHTMLMessageBody(messageBody);
        } else {
            this.body = MessageHelper.prepareMessageBody(messageBody);
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
     */
    public void prepareMessage(String hostName, AuditLogEvent event,
                               boolean isBodyHtml) {
        prepareMessageSubject(hostName, event);
        prepareMessageBody(event, isBodyHtml);
    }

}

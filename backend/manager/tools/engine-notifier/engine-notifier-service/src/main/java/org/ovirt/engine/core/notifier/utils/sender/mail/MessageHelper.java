package org.ovirt.engine.core.notifier.utils.sender.mail;

import org.apache.commons.lang.StringUtils;

/**
 * A helper class designed to construct message parts in static structure
 */
public class MessageHelper {
    private static String USER_INFO = "User Name: %s\n";
    private static String VM_INFO = "VM Name: %s\n";
    private static String HOST_INFO = "Host Name: %s\n";
    private static String TEMPLATE_INFO = "Template Name: %s\n";
    private static String DATA_CENTER_INFO = "Data Center Name: %s\n";
    private static String STORAGE_DOMAIN_INFO = "Storage Domain Name: %s\n";

    private static String HTML_USER_INFO = "<b>User Name:</b> %s<br>";
    private static String HTML_VM_INFO = "<b>VM Name:</b> %s<br>";
    private static String HTML_HOST_INFO = "<b>Host Name:</b> %s<br>";
    private static String HTML_TEMPLATE_INFO = "<b>Template Name:</b> %s<br>";
    private static String HTML_DATA_CENTER_INFO = "<b>Data Center Name:</b> %s<br>";
    private static String HTML_STORAGE_DOMAIN_INFO = "<b>Storage Domain Name:</b> %s<br>";

    /**
     * Describes a message type set by event type and associate message.
     */
    static public enum MessageType {

        resolveMessage(0, "Issue Solved Notification."),
        alertMessage(1, "Alert Notification.");

        private String message;
        private int eventType;

        private MessageType(int eventType, String message) {
            this.message = message;
            this.eventType = eventType;
        }

        public String getMessage() {
            return message;
        }

        public int getEventType() {
            return eventType;
        }

        static public String getMessageByEventType(int eventType) {
            for (MessageHelper.MessageType m : values()) {
                if (eventType == m.eventType) {
                    return m.message;
                }
            }
            return null;
        }
    }

    /**
     * Constructs a formatted message body based on provided message body elements content.<br>
     * If any of message body element is empty or missing, it will not appear in the formatted message body text
     * @param messageBody
     *            the message body values for populate a formatted message body
     * @return a formatted message body
     */
    public static String prepareMessageBody(MessageBody messageBody) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Time:%s\nMessage:%s\nSeverity:%s\n",
                messageBody.getLogTime(),
                messageBody.getMessage(),
                messageBody.getSeverity()));

        if (StringUtils.isNotEmpty(messageBody.getUserInfo())) {
            sb.append(String.format(USER_INFO, messageBody.getUserInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getVmInfo())) {
            sb.append(String.format(VM_INFO, messageBody.getVmInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getHostInfo())) {
            sb.append(String.format(HOST_INFO, messageBody.getHostInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getTemplateInfo())) {
            sb.append(String.format(TEMPLATE_INFO, messageBody.getTemplateInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getDatacenterInfo())) {
            sb.append(String.format(DATA_CENTER_INFO, messageBody.getDatacenterInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getStorageDomainInfo())) {
            sb.append(String.format(STORAGE_DOMAIN_INFO, messageBody.getStorageDomainInfo()));
        }
        return sb.toString();
    }

    /**
     * Construct a formatted message based on predefined template:<br>
     * {@code "Issue Solved"/"Alert Notification" (host name), [message details]}
     * @param eventType
     *            determines the prefix of the subject
     * @param hostName
     *            the machine names associated with this event
     * @param message
     *            the content of the message to convey
     * @return a formatted message subject
     */
    public static String prepareMessageSubject(int eventType, String hostName, String message) {
        return String.format("%s (%s), [%s]", MessageType.getMessageByEventType(eventType), hostName, message);

    }

    /**
     * Constructs a formatted message body based on provided message body elements content in HTML format.<br>
     * If any of message body element is empty or missing, it will not appear in the formatted message body text
     * @param messageBody
     *            the message body values for populate a formatted message body
     * @return a formatted HTML message body
     */
    public static String prepareHTMLMessageBody(MessageBody messageBody) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("<b>Time:</b> %s<br><b>Message:</b> %s<br><b>Severity:</b> %s<p>",
                messageBody.getLogTime(),
                messageBody.getMessage(),
                messageBody.getSeverity()));

        if (StringUtils.isNotEmpty(messageBody.getUserInfo())) {
            sb.append(String.format(HTML_USER_INFO, messageBody.getUserInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getVmInfo())) {
            sb.append(String.format(HTML_VM_INFO, messageBody.getVmInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getHostInfo())) {
            sb.append(String.format(HTML_HOST_INFO, messageBody.getHostInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getTemplateInfo())) {
            sb.append(String.format(HTML_TEMPLATE_INFO, messageBody.getTemplateInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getDatacenterInfo())) {
            sb.append(String.format(HTML_DATA_CENTER_INFO, messageBody.getDatacenterInfo()));
        }
        if (StringUtils.isNotEmpty(messageBody.getStorageDomainInfo())) {
            sb.append(String.format(HTML_STORAGE_DOMAIN_INFO, messageBody.getStorageDomainInfo()));
        }
        return sb.toString();
    }
}

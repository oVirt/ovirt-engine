package org.ovirt.engine.core.notifier.transport.smtp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.notifier.filter.AuditLogEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class designed to construct localized message parts in static structure
 */
public class LocalizedMessageHelper {
    private static final Logger log = LoggerFactory.getLogger(LocalizedMessageHelper.class);
    private static final Map<Locale, ResourceBundle> RESOURCES = new ConcurrentHashMap<>();

    /**
     * Constructs a formatted message body based on provided message body elements content.<br>
     * If any of message body element is empty or missing, it will not appear in the formatted message body text
     * @param messageBody
     *            the message body values for populate a formatted message body
     * @param locale
     *            locale for the message content
     * @return a formatted message body
     */
    public static String prepareMessageBody(MessageBody messageBody, Locale locale) {
        StringBuilder sb = new StringBuilder();

        appendPlainMessage(sb, "smtp.message.body.plain.time", getLogTime(messageBody, locale), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.message", messageBody.getMessage(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.severity", getSeverity(messageBody, locale), locale);

        appendPlainMessage(sb, "smtp.message.body.plain.user.info", messageBody.getUserInfo(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.vm.info", messageBody.getVmInfo(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.host.info", messageBody.getHostInfo(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.template.info", messageBody.getTemplateInfo(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.dc.info", messageBody.getDatacenterInfo(), locale);
        appendPlainMessage(sb, "smtp.message.body.plain.storage.domain.info", messageBody.getStorageDomainInfo(), locale);

        return sb.toString();
    }

    /**
     * Construct a formatted message based on predefined template:<br>
     * {@code "Issue Solved"/"Alert Notification" (host name), [message details]}
     * @param type
     *            determines the prefix of the subject
     * @param hostName
     *            the machine names associated with this event
     * @param message
     *            the content of the message to convey
     * @param locale
     *            locale for the message content
     * @return a formatted message subject
     */
    public static String prepareMessageSubject(AuditLogEventType type, String hostName, String message, Locale locale) {
        String auditLogEventType = type.name();
        switch (type) {
            case alertMessage:
                auditLogEventType = getResourceString("smtp.message.audit.log.event.type.alert", locale, auditLogEventType);
                break;
            case resolveMessage:
                auditLogEventType = getResourceString("smtp.message.audit.log.event.type.resolve", locale, auditLogEventType);
                break;
        }
        return String.format("%s (%s), [%s]", auditLogEventType, hostName, message);

    }

    /**
     * Constructs a formatted message body based on provided message body elements content in HTML format.<br>
     * If any of message body element is empty or missing, it will not appear in the formatted message body text
     * @param messageBody
     *            the message body values for populate a formatted message body
     * @param locale
     *            locale for the message content
     * @return a formatted HTML message body
     */
    public static String prepareHTMLMessageBody(MessageBody messageBody, Locale locale) {
        StringBuilder sb = new StringBuilder();

        appendHtmlMessage(sb, "smtp.message.body.html.time", getLogTime(messageBody, locale), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.message", messageBody.getMessage(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.severity", getSeverity(messageBody, locale), locale);

        appendHtmlMessage(sb, "smtp.message.body.html.user.info", messageBody.getUserInfo(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.vm.info", messageBody.getVmInfo(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.host.info", messageBody.getHostInfo(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.template.info", messageBody.getTemplateInfo(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.dc.info", messageBody.getDatacenterInfo(), locale);
        appendHtmlMessage(sb, "smtp.message.body.html.storage.domain.info", messageBody.getStorageDomainInfo(), locale);

        return sb.toString();
    }

    private static String getLogTime(MessageBody messageBody, Locale locale) {
        Date logTime = messageBody.getLogTime();
        if (logTime == null) {
            return "";
        }
        return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale).format(logTime);
    }

    private static String getSeverity(MessageBody messageBody, Locale locale) {
        AuditLogSeverity severity = messageBody.getSeverity();
        if (severity == null) {
            return "";
        }
        String auditLogSeverity = severity.name();
        switch (severity) {
            case NORMAL:
                auditLogSeverity = getResourceString("smtp.message.audit.log.severity.normal", locale, auditLogSeverity);
                break;
            case WARNING:
                auditLogSeverity = getResourceString("smtp.message.audit.log.severity.warning", locale, auditLogSeverity);
                break;
            case ERROR:
                auditLogSeverity = getResourceString("smtp.message.audit.log.severity.error", locale, auditLogSeverity);
                break;
            case ALERT:
                auditLogSeverity = getResourceString("smtp.message.audit.log.severity.alert", locale, auditLogSeverity);
                break;

        }
        return auditLogSeverity;
    }

    private static void appendPlainMessage(StringBuilder body, String messageId, String messageValue, Locale locale) {
        appendMessage(body, messageId, messageValue, System.lineSeparator(), locale);
    }
    private static void appendHtmlMessage(StringBuilder body, String messageId, String messageValue, Locale locale) {
        appendMessage(body, messageId, messageValue, "<br>", locale);
    }
    private static void appendMessage(StringBuilder body, String messageId, String messageValue, String messageSuffix, Locale locale) {
        if (StringUtils.isNotEmpty(messageValue)) {
            String message = getResourceString(messageId, locale, "");
            body.append(String.format(message, messageValue))
                    .append(messageSuffix);
        }
    }

    private static String getResourceString(String id, Locale locale, String defaultValue) {
        try {
            return RESOURCES.computeIfAbsent(locale, lc -> ResourceBundle.getBundle("smtp-messages", lc))
                    .getString(id);
        } catch (MissingResourceException mre) {
            log.debug("Failed to load resource string {}: {}", id, mre.getMessage(), mre);
            return defaultValue;
        }
    }
}

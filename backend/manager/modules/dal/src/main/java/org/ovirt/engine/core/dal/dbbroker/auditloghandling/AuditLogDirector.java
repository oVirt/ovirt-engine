package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuditLogDirector {
    private static final Logger log = LoggerFactory.getLogger(AuditLogDirector.class);
    private static final Pattern pattern = Pattern.compile("\\$\\{\\w*\\}"); // match ${<alphanumeric>...}
    private static final int USERNAME_LENGTH = 255;
    static final String UNKNOWN_VARIABLE_VALUE = "<UNKNOWN>";
    static final String UNKNOWN_REASON_VALUE = " No reason was returned for this operation failure. See logs for further details.";
    static final String REASON_TOKEN = "reason";
    static final String OPTIONAL_REASON_TOKEN = "optionalreason";
    private static final ResourceBundle resourceBundle = getResourceBundle();

    static ResourceBundle getResourceBundle() {
        try {
            return ResourceBundle.getBundle(getResourceBundleName());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Could not find ResourceBundle file '" + getResourceBundleName() +"'.");
        }
    }

    static String getResourceBundleName() {
        return "bundles/AuditLogMessages";
    }

    public static String getMessage(AuditLogType logType) {
        return StringUtils.defaultString(getMessageOrNull(logType));
    }

    protected static String getMessageOrNull(AuditLogType logType) {
        final String key = logType.name();
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            log.error("Key '{}' is not translated in '{}'", key, getResourceBundleName());
            return null;
        }
    }

    public void log(AuditLogableBase auditLogable) {
        AuditLogType logType = auditLogable.getAuditLogTypeValue();
        log(auditLogable, logType);
    }

    public void log(AuditLogable auditLogable, AuditLogType logType) {
        log(auditLogable, logType, "");
    }

    public void log(AuditLogable auditLogable, AuditLogType logType, String loggerString) {
        if (!logType.shouldBeLogged()) {
            return;
        }

        EventFloodRegulator eventFloodRegulator = new EventFloodRegulator(auditLogable, logType);
        if (eventFloodRegulator.isLegal()) {
            saveToDb(auditLogable, logType, loggerString);
        }
    }

    private void saveToDb(AuditLogable auditLogable, AuditLogType logType, String loggerString) {
        AuditLogSeverity severity = logType.getSeverity();
        AuditLog auditLog = createAuditLog(auditLogable, logType, loggerString, severity);

        if (auditLog == null) {
            log.warn("Unable to create AuditLog");
        } else {
            auditLogable.setPropertiesForAuditLog(auditLog);
            // truncate user name
            auditLog.setUserName(StringUtils.abbreviate(auditLog.getUserName(), USERNAME_LENGTH));
            getDbFacadeInstance().getAuditLogDao().save(auditLog);
            logMessage(severity, getMessageToLog(loggerString, auditLog));
        }
    }

    private void logMessage(AuditLogSeverity severity, String logMessage) {
        switch (severity) {
        case NORMAL:
            log.info(logMessage);
            break;
        case ERROR:
            log.error(logMessage);
            break;
        case ALERT:
        case WARNING:
        default:
            log.warn(logMessage);
            break;
        }
    }

    private static String getMessageToLog(String loggerString, AuditLog auditLog) {
        String message;
        if (loggerString.isEmpty()) {
            message = auditLog.toStringForLogging();
        } else {
            message = MessageFormat.format(loggerString, auditLog.getMessage());
        }
        return MessageFormat.format("EVENT_ID: {0}({1}), {2}",
                auditLog.getLogType(),
                auditLog.getLogType().getValue(), message);
    }

    private AuditLog createAuditLog(AuditLogable auditLogable, AuditLogType logType, String loggerString, AuditLogSeverity severity) {
        // handle external log messages invoked by plugins via the API
        if (auditLogable.isExternal()) {
            return auditLogable.createAuditLog(logType, severity, loggerString);
        }

        final String messageByType = getMessageOrNull(logType);
        if (messageByType == null) {
            return null;
        } else {
            // Application log message from AuditLogMessages
            String resolvedMessage = resolveMessage(messageByType, auditLogable);
            return auditLogable.createAuditLog(logType, severity, resolvedMessage);
        }
    }

    private DbFacade getDbFacadeInstance() {
        return DbFacade.getInstance();
    }

    String resolveMessage(String message, AuditLogable logable) {
        String returnValue = message;
        if (logable != null) {
            Map<String, String> map = getAvailableValues(message, logable);
            returnValue = resolveMessage(message, map);
        }
        return returnValue;
    }

    /**
     * Resolves a message which contains place holders by replacing them with the value from the map.
     *
     * @param message
     *            A text representing a message with place holders
     * @param values
     *            a map of the place holder to its values
     * @return a resolved message
     */
    public String resolveMessage(String message, Map<String, String> values) {
        Matcher matcher = pattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        String value;
        String token;
        while (matcher.find()) {
            token = matcher.group();

            // remove leading ${ and trailing }
            token = token.substring(2, token.length() - 1);

            // get value from value map
            value = values.get(token.toLowerCase());
            if (value == null || value.isEmpty()) {
                // replace value with UNKNOWN_VARIABLE_VALUE if value not defined
                switch(token.toLowerCase()) {
                    case REASON_TOKEN:
                        value = UNKNOWN_REASON_VALUE;
                        break;
                    case OPTIONAL_REASON_TOKEN:
                        value = "";
                        break;
                    default:
                        value = UNKNOWN_VARIABLE_VALUE;
                }
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value)); // put the value into message
        }

        // append the rest of the message
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Set<String> resolvePlaceHolders(String message) {
        Set<String> result = new HashSet<>();
        Matcher matcher = pattern.matcher(message);

        String token;
        while (matcher.find()) {
            token = matcher.group();

            // remove leading ${ and trailing }
            token = token.substring(2, token.length() - 1);
            result.add(token.toLowerCase());
        }
        return result;
    }

    private Map<String, String> getAvailableValues(String message, AuditLogable logable) {
        Map<String, String> returnValue = new HashMap<>(logable.getCustomValues());
        Set<String> attributes = resolvePlaceHolders(message);
        if (attributes != null && attributes.size() > 0) {
            TypeCompat.getPropertyValues(logable, attributes, returnValue);
        }
        return returnValue;
    }
}

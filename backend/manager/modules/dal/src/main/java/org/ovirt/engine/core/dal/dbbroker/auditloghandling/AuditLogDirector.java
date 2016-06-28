package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
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

    public void log(AuditLogableBase auditLogable, AuditLogType logType) {
        log(auditLogable, logType, "");
    }

    public void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        if (!logType.shouldBeLogged()) {
            return;
        }

        updateTimeoutLogableObject(auditLogable, logType);

        if (auditLogable.getLegal()) {
            saveToDb(auditLogable, logType, loggerString);
        }
    }

    private void saveToDb(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        AuditLogSeverity severity = logType.getSeverity();
        AuditLog auditLog = createAuditLog(auditLogable, logType, loggerString, severity);

        if (auditLog == null) {
            log.warn("Unable to create AuditLog");
        } else {
            setPropertiesFromAuditLogableBase(auditLogable, auditLog);
            // truncate user name
            auditLog.setUserName(StringUtils.abbreviate(auditLog.getUserName(), USERNAME_LENGTH));
            getDbFacadeInstance().getAuditLogDao().save(auditLog);
            logMessage(severity, getMessageToLog(loggerString, auditLog));
        }
    }

    private static void setPropertiesFromAuditLogableBase(AuditLogableBase auditLogable, AuditLog auditLog) {
        auditLog.setStorageDomainId(auditLogable.getStorageDomainId());
        auditLog.setStorageDomainName(auditLogable.getStorageDomainName());
        auditLog.setStoragePoolId(auditLogable.getStoragePoolId());
        auditLog.setStoragePoolName(auditLogable.getStoragePoolName());
        auditLog.setClusterId(auditLogable.getClusterId());
        auditLog.setClusterName(auditLogable.getClusterName());
        auditLog.setCorrelationId(auditLogable.getCorrelationId());
        auditLog.setJobId(auditLogable.getJobId());
        auditLog.setGlusterVolumeId(auditLogable.getGlusterVolumeId());
        auditLog.setGlusterVolumeName(auditLogable.getGlusterVolumeName());
        auditLog.setExternal(auditLogable.isExternal());
        auditLog.setQuotaId(auditLogable.getQuotaIdForLog());
        auditLog.setQuotaName(auditLogable.getQuotaNameForLog());
        auditLog.setCallStack(auditLogable.getCallStack());
        auditLog.setBrickId(auditLogable.getBrickId());
        auditLog.setBrickPath(auditLogable.getBrickPath());
        auditLog.setRepeatable(auditLogable.isRepeatable());
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
        if (loggerString.isEmpty()) {
            return auditLog.toStringForLogging();
        } else {
            return MessageFormat.format(loggerString, auditLog.getMessage());
        }
    }

    private AuditLog createAuditLog(AuditLogableBase auditLogable, AuditLogType logType, String loggerString, AuditLogSeverity severity) {
        // handle external log messages invoked by plugins via the API
        if (auditLogable.isExternal()) {
            String resolvedMessage = loggerString; // message is sent as an argument, no need to resolve.
            return new AuditLog(logType,
                    severity,
                    resolvedMessage,
                    auditLogable.getUserId(),
                    auditLogable.getUserName(),
                    auditLogable.getVmIdRef(),
                    auditLogable.getVmIdRef() != null ? getDbFacadeInstance().getVmDao().get(auditLogable.getVmIdRef()).getName() : null,
                    auditLogable.getVdsIdRef(),
                    auditLogable.getVdsIdRef() != null ? getDbFacadeInstance().getVdsDao().get(auditLogable.getVdsIdRef()).getName() : null,
                    auditLogable.getVmTemplateIdRef(),
                    auditLogable.getVmTemplateIdRef() != null ? getDbFacadeInstance().getVmTemplateDao().get(auditLogable.getVmTemplateIdRef()).getName() : null,
                    auditLogable.getOrigin(),
                    auditLogable.getCustomEventId(),
                    auditLogable.getEventFloodInSec(),
                    auditLogable.getCustomData());
        }

        final String messageByType = getMessageOrNull(logType);
        if (messageByType == null) {
            return null;
        } else {
            // Application log message from AuditLogMessages
            String resolvedMessage = resolveMessage(messageByType, auditLogable);
            return new AuditLog(logType, severity, resolvedMessage, auditLogable.getUserId(),
                    auditLogable.getUserName(), auditLogable.getVmIdRef(), auditLogable.getVmName(),
                    auditLogable.getVdsIdRef(), auditLogable.getVdsName(), auditLogable.getVmTemplateIdRef(),
                    auditLogable.getVmTemplateName());
        }
    }

    /**
     * Update the logged object timeout attribute by log type definition
     * @param auditLogable
     *            the logable object to be updated
     * @param logType
     *            the log type which determine if timeout is used for it
     */
    private void updateTimeoutLogableObject(AuditLogableBase auditLogable, AuditLogType logType) {
        int eventFloodRate = (auditLogable.isExternal() && auditLogable.getEventFloodInSec() == 0)
                ?
                30 // Minimal default duration for External Events is 30 seconds.
                :
                logType.getEventFloodRate();
        if (eventFloodRate > 0) {
            auditLogable.setEndTime(TimeUnit.SECONDS.toMillis(eventFloodRate));
            auditLogable.setTimeoutObjectId(composeObjectId(auditLogable, logType));
        }
    }

    private DbFacade getDbFacadeInstance() {
        return DbFacade.getInstance();
    }

    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     * @param logable
     *                the object to log
     * @param logType
     *                the log type associated with the object
     * @param userId
     *                the userid to be composed
     * @return unique object id
     */
    private String composeObjectId(AuditLogableBase logable, AuditLogType logType, Guid userId) {
        final StringBuilder builder = new StringBuilder();

        compose(builder, "type", logType.toString());
        compose(builder, "sd", nullToEmptyString(logable.getStorageDomainId()));
        compose(builder, "dc", nullToEmptyString(logable.getStoragePoolId()));
        compose(builder, "user", nullToEmptyString(userId));
        compose(builder, "cluster", logable.getClusterId().toString());
        compose(builder, "vds", logable.getVdsId().toString());
        compose(builder, "vm", emptyGuidToEmptyString(logable.getVmId()));
        compose(builder, "template", emptyGuidToEmptyString(logable.getVmTemplateId()));
        compose(builder, "customId", StringUtils.defaultString(logable.getCustomId()));

        return builder.toString();
    }
    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     * @param logable
     *            the object to log
     * @param logType
     *            the log type associated with the object
     * @return a unique object id
     */
    private String composeObjectId(AuditLogableBase logable, AuditLogType logType) {
        return composeObjectId(logable, logType, logable.getUserId());
    }

    /**
     * Composes an system object id from all log id's to identify uniquely each instance.
     * @param logable
     *            the object to log
     * @param logType
     *            the log type associated with the object
     * @return a unique object id
     */
    public String composeSystemObjectId(AuditLogableBase logable, AuditLogType logType) {
        return composeObjectId(logable, logType, Guid.Empty);
    }

    private void compose(StringBuilder builder, String key, String value) {
        final char DELIMITER = ',';
        final char NAME_VALUE_SEPARATOR = '=';
        if (builder.length() > 0) {
            builder.append(DELIMITER);
        }

        builder.append(key).append(NAME_VALUE_SEPARATOR).append(value);
    }

    private String emptyGuidToEmptyString(Guid guid) {
        return guid.equals(Guid.Empty) ? "" : guid.toString();
    }

    private static String nullToEmptyString(Object obj) {
        return Objects.toString(obj, "");
    }

    String resolveMessage(String message, AuditLogableBase logable) {
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

    private Map<String, String> getAvailableValues(String message, AuditLogableBase logable) {
        Map<String, String> returnValue = new HashMap<>(logable.getCustomValues());
        Set<String> attributes = resolvePlaceHolders(message);
        if (attributes != null && attributes.size() > 0) {
            TypeCompat.getPropertyValues(logable, attributes, returnValue);
        }
        return returnValue;
    }
}

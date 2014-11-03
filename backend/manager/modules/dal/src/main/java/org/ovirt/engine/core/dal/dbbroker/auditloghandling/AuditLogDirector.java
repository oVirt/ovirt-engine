package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class AuditLogDirector {
    private static final Logger log = LoggerFactory.getLogger(AuditLogDirector.class);
    private static final Map<AuditLogType, String> messages = new EnumMap<AuditLogType, String>(AuditLogType.class);
    private static final Pattern pattern = Pattern.compile("\\$\\{\\w*\\}"); // match ${<alphanumeric>...}
    static final String UNKNOWN_VARIABLE_VALUE = "<UNKNOWN>";
    static final String UNKNOWN_REASON_VALUE = "Not Specified";
    static final String REASON_TOKEN = "reason";
    private static final String APP_ERRORS_MESSAGES_FILE_NAME = "bundles/AuditLogMessages";

    static {
        initMessages();
    }

    private static void initMessages() {
        ResourceBundle bundle = readMessagesFromBundle();

        for (String key : bundle.keySet()) {
            try {
                AuditLogType type = AuditLogType.valueOf(key);
                if (!messages.containsKey(type)) {
                    messages.put(type, bundle.getString(key));
                } else {
                    log.error("The type '{}' appears more then once in audit log messages bundle with the values"
                                    + " '{}' and '{}'",
                            type,
                            messages.get(type),
                            bundle.getString(key));
                }
            } catch (Exception e) {
                log.error("Cannot convert the string '{}' to AuditLogType, the key does not exist in the AuditLogType"
                                + " declared types",
                        bundle.getString(key));
            }
        }
        checkMessages();
    }

    private static ResourceBundle readMessagesFromBundle() {
        try {
            return ResourceBundle.getBundle(APP_ERRORS_MESSAGES_FILE_NAME);
        } catch (MissingResourceException e) {
            log.error("Could not load audit log messages from the file '{}'", APP_ERRORS_MESSAGES_FILE_NAME);
            throw e;
        }
    }

    private static void checkMessages() {
        AuditLogType[] values = AuditLogType.values();
        if (values.length != messages.size()) {
            for (AuditLogType value : values) {
                if (!messages.containsKey(value)) {
                    log.info("AuditLogType: '{}' not exist in string table", value);
                }
            }
        }
    }

    /**
     * Gets the message.
     * @param logType
     *            Type of the log.
     * @return
     */
    public static String getMessage(AuditLogType logType) {
        String value = "";
        if (messages.containsKey(logType)) {
            value = messages.get(logType);
        }
        return value;
    }

    public static void log(AuditLogableBase auditLogable) {
        AuditLogType logType = auditLogable.getAuditLogTypeValue();
        log(auditLogable, logType);
    }

    public static void log(AuditLogableBase auditLogable, AuditLogType logType) {
        log(auditLogable, logType, "");
    }

    public static void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        updateTimeoutLogableObject(auditLogable, logType);

        if (auditLogable.getLegal()) {
            saveToDb(auditLogable, logType, loggerString);
        }
    }

    private static void saveToDb(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        String message = null;
        String resolvedMessage = null;
        AuditLogSeverity severity = logType.getSeverity();
        if (severity == null) {
            severity = AuditLogSeverity.NORMAL;
            log.info("No severity for '{}' audit log type, assuming Normal severity", logType);
        }
        AuditLog auditLog = null;
        // handle external log messages invoked by plugins via the API
        if (auditLogable.isExternal()) {
            resolvedMessage = message = loggerString; // message is sent as an argument, no need to resolve.
            auditLog = new AuditLog(logType,
                    severity,
                    resolvedMessage,
                    auditLogable.getUserId(),
                    auditLogable.getUserId() != null ? getDbFacadeInstance().getDbUserDao().get(auditLogable.getUserId()).getLoginName() : null,
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
        } else if ((message = messages.get(logType)) != null) { // Application log message from AuditLogMessages
            resolvedMessage = resolveMessage(message, auditLogable);
            auditLog = new AuditLog(logType, severity, resolvedMessage, auditLogable.getUserId(),
                    auditLogable.getUserName(), auditLogable.getVmIdRef(), auditLogable.getVmName(),
                    auditLogable.getVdsIdRef(), auditLogable.getVdsName(), auditLogable.getVmTemplateIdRef(),
                    auditLogable.getVmTemplateName());
        }
        if (auditLog != null) {
            auditLog.setStorageDomainId(auditLogable.getStorageDomainId());
            auditLog.setStorageDomainName(auditLogable.getStorageDomainName());
            auditLog.setStoragePoolId(auditLogable.getStoragePoolId());
            auditLog.setStoragePoolName(auditLogable.getStoragePoolName());
            auditLog.setVdsGroupId(auditLogable.getVdsGroupId());
            auditLog.setVdsGroupName(auditLogable.getVdsGroupName());
            auditLog.setCorrelationId(auditLogable.getCorrelationId());
            auditLog.setJobId(auditLogable.getJobId());
            auditLog.setGlusterVolumeId(auditLogable.getGlusterVolumeId());
            auditLog.setGlusterVolumeName(auditLogable.getGlusterVolumeName());
            auditLog.setExternal(auditLogable.isExternal());
            auditLog.setQuotaId(auditLogable.getQuotaIdForLog());
            auditLog.setQuotaName(auditLogable.getQuotaNameForLog());
            auditLog.setCallStack(auditLogable.getCallStack());
            getDbFacadeInstance().getAuditLogDao().save(auditLog);
            String logMessage;
            if (!"".equals(loggerString)) {
                logMessage = MessageFormat.format(loggerString.replaceAll("'", ""), resolvedMessage);
            } else {
                logMessage = auditLog.toStringForLogging();
            }

            switch (severity) {
            case NORMAL:
                log.info(logMessage);
                break;
            case ERROR:
                log.error(logMessage);
                break;
            case ALERT:
            case WARNING:
                log.warn(logMessage);
                break;
            }

        }
    }

    /**
     * Update the logged object timeout attribute by log type definition
     * @param auditLogable
     *            the logable object to be updated
     * @param logType
     *            the log type which determine if timeout is used for it
     */
    private static void updateTimeoutLogableObject(AuditLogableBase auditLogable, AuditLogType logType) {
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

    public static DbFacade getDbFacadeInstance() {
        return DbFacade.getInstance();
    }

    /**
     * Composes an object id from all log id's to identify uniquely each instance.
     * @param logable
     *            the object to log
     * @param logType
     *            the log type associated with the object
     * @return a unique object id
     */
    private static String composeObjectId(AuditLogableBase logable, AuditLogType logType) {
        final char DELIMITER = ',';
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(logType);
        sb.append(DELIMITER);
        sb.append("sd=");
        sb.append(logable.getStorageDomainId() == null ? "" : logable.getStorageDomainId().toString());
        sb.append(DELIMITER);
        sb.append("dc=");
        sb.append(logable.getStoragePoolId() == null ? "" : logable.getStoragePoolId().toString());
        sb.append(DELIMITER);
        sb.append("user=");
        sb.append(logable.getUserId() == null ? "" : logable.getUserId().toString());
        sb.append(DELIMITER);
        sb.append("cluster=");
        sb.append(logable.getVdsGroupId().toString());
        sb.append(DELIMITER);
        sb.append("vds=");
        sb.append(logable.getVdsId().toString());
        sb.append(DELIMITER);
        sb.append("vm=");
        sb.append(logable.getVmId().equals(Guid.Empty) ? "" : logable.getVmId().toString());
        sb.append(DELIMITER);
        sb.append("template=");
        sb.append(logable.getVmTemplateId().equals(Guid.Empty) ? "" : logable.getVmTemplateId().toString());
        sb.append(DELIMITER);
        sb.append("customId=");
        sb.append(StringUtils.defaultString(logable.getCustomId()));
        sb.append(DELIMITER);

        return sb.toString();
    }

    static String resolveMessage(String message, AuditLogableBase logable) {
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
    public static String resolveMessage(String message, Map<String, String> values) {
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

    private static Set<String> resolvePlaceHolders(String message) {
        Set<String> result = new HashSet<String>();
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

    private static Map<String, String> getAvailableValues(String message, AuditLogableBase logable) {
        Map<String, String> returnValue = new HashMap<String, String>(logable.getCustomValues());
        Set<String> attributes = resolvePlaceHolders(message);
        if (attributes != null && attributes.size() > 0) {
            TypeCompat.getPropertyValues(logable, attributes, returnValue);
        }
        return returnValue;
    }
}

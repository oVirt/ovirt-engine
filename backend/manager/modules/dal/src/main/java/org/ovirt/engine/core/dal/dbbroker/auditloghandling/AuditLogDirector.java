package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.text.MessageFormat;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AuditLogDirector {
    private static final Logger log = LoggerFactory.getLogger(AuditLogDirector.class);
    private static final int USERNAME_LENGTH = 255;

    public void log(AuditLogable auditLogable, AuditLogType logType) {
        log(auditLogable, logType, "");
    }

    private AuditLogDirector() {
    }

    /**
     * Log an event with the given message
     *
     * @param auditLogable
     *            the event which contains the data members to log
     * @param logType
     *            the log type to be logged
     * @param message
     *            the message to be logged, which overrides the calculated message provided by the given auditLogable
     */
    public void log(AuditLogable auditLogable, AuditLogType logType, String message) {
        if (!logType.shouldBeLogged()) {
            return;
        }

        EventFloodRegulator eventFloodRegulator = new EventFloodRegulator(auditLogable, logType);
        if (eventFloodRegulator.isLegal()) {
            AuditLog savedAuditLog = saveToDb(auditLogable, logType, message);
            if (savedAuditLog == null) {
                log.warn("Unable to create AuditLog");
            } else {
                logMessage(savedAuditLog);
            }
        }
    }

    private AuditLog saveToDb(AuditLogable auditLogable, AuditLogType logType, String loggerString) {
        AuditLog auditLog = create(auditLogable, logType, loggerString);

        if (auditLog == null) {
            return null;
        }

        auditLogable.setPropertiesForAuditLog(auditLog);
        // truncate user name
        auditLog.setUserName(StringUtils.abbreviate(auditLog.getUserName(), USERNAME_LENGTH));
        getDbFacadeInstance().getAuditLogDao().save(auditLog);
        return auditLog;
    }

    private void logMessage(AuditLog auditLog) {
        String logMessage = getMessageToLog(auditLog);
        switch (auditLog.getSeverity()) {
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

    private String getMessageToLog(AuditLog auditLog) {
        return MessageFormat.format("EVENT_ID: {0}({1}), {2}",
                auditLog.getLogType(),
                auditLog.getLogType().getValue(), auditLog.getMessage());
    }

    private AuditLog create(AuditLogable auditLogable, AuditLogType logType, String loggerString) {
        // handle external log messages invoked by plugins via the API
        if (auditLogable.isExternal()) {
            return auditLogable.createAuditLog(logType, loggerString);
        }

        final String messageByType = MessageBundler.getMessageOrNull(logType);
        if (messageByType == null) {
            return null;
        } else {
            // Application log message from AuditLogMessages
            String resolvedMessage = MessageResolver.resolveMessage(messageByType, auditLogable);
            return auditLogable.createAuditLog(logType, resolvedMessage);
        }
    }

    private DbFacade getDbFacadeInstance() {
        return DbFacade.getInstance();
    }
}

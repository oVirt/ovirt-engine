package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.common.AuditLogType;

public final class AuditLogDirectorDelegator implements AuditLogger {

    private static final AuditLogger instance = new AuditLogDirectorDelegator();
    private final AuditLogDirector auditLogDirector = new AuditLogDirector();

    public static AuditLogger getInstance() {
        return instance;
    }

    @Override
    public void log(AuditLogableBase auditLogable) {
        auditLogDirector.log(auditLogable);
    }

    @Override
    public void log(AuditLogableBase auditLogable, AuditLogType logType) {
        auditLogDirector.log(auditLogable, logType);
    }

    @Override
    public void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        auditLogDirector.log(auditLogable, logType, loggerString);
    }
}

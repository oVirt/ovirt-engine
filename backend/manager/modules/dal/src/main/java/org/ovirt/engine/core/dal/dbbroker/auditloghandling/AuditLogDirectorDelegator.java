package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.common.AuditLogType;

public final class AuditLogDirectorDelegator implements AuditLogger {

    private static final AuditLogger instance = new AuditLogDirectorDelegator();

    public static AuditLogger getInstance() {
        return instance;
    }

    @Override
    public void log(AuditLogableBase auditLogable) {
        AuditLogDirector.log(auditLogable);
    }

    @Override
    public void log(AuditLogableBase auditLogable, AuditLogType logType) {
        AuditLogDirector.log(auditLogable, logType);
    }

    @Override
    public void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString) {
        AuditLogDirector.log(auditLogable, logType, loggerString);
    }
}

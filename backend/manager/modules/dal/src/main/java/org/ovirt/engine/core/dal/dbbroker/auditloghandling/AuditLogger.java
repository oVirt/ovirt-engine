package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.common.AuditLogType;

public interface AuditLogger {

    void log(AuditLogableBase auditLogable);

    void log(AuditLogableBase auditLogable, AuditLogType logType);

    void log(AuditLogableBase auditLogable, AuditLogType logType, String loggerString);

}

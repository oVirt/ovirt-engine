package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public class RemoveAuditLogByIdParameters extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = 7211692656127711421L;
    private long auditLogId;

    public RemoveAuditLogByIdParameters() {}

    public RemoveAuditLogByIdParameters(long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public void setAuditLogId(long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public long getAuditLogId() {
        return auditLogId;
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;

public class UploadImageParameters extends ImagesActionsParametersBase {
    private static final long serialVersionUID = -3924328349293932142L;

    private int keepaliveInterval;
    private long uploadSize;

    // Members used to persist data during command execution
    long sessionExpiration;
    AuditLogType auditLogType;
    long lastPauseLogTime;

    public UploadImageParameters() {
    }

    public UploadImageParameters(Guid storageDomainId, int keepaliveInterval) {
        setStorageDomainId(storageDomainId);
        setKeepaliveInterval(keepaliveInterval);
    }

    public int getKeepaliveInterval() {
        return keepaliveInterval;
    }

    public void setKeepaliveInterval(int keepaliveInterval) {
        this.keepaliveInterval = keepaliveInterval;
    }

    public long getUploadSize() {
        return uploadSize;
    }

    public void setUploadSize(long uploadSize) {
        this.uploadSize = uploadSize;
    }

    public long getSessionExpiration() {
        return sessionExpiration;
    }

    public void setSessionExpiration(long sessionExpiration) {
        this.sessionExpiration = sessionExpiration;
    }

    public AuditLogType getAuditLogType() {
        return auditLogType;
    }

    public void setAuditLogType(AuditLogType auditLogType) {
        this.auditLogType = auditLogType;
    }

    public long getLastPauseLogTime() {
        return lastPauseLogTime;
    }

    public void setLastPauseLogTime(long lastPauseLogTime) {
        this.lastPauseLogTime = lastPauseLogTime;
    }
}

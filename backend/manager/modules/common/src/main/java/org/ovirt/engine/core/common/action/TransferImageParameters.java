package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.compat.Guid;

public class TransferImageParameters extends ImagesActionsParametersBase {
    private static final long serialVersionUID = -3924328349293932142L;

    private int keepaliveInterval;
    private long transferSize;
    private TransferType transferType = TransferType.Upload;

    // Members used to persist data during command execution
    long sessionExpiration;
    AuditLogType auditLogType;
    long lastPauseLogTime;
    String downloadFilename;
    boolean retryExtendTicket = true;

    public TransferImageParameters() {
    }

    public TransferImageParameters(Guid storageDomainId, int keepaliveInterval) {
        setStorageDomainId(storageDomainId);
        setKeepaliveInterval(keepaliveInterval);
    }

    public int getKeepaliveInterval() {
        return keepaliveInterval;
    }

    public void setKeepaliveInterval(int keepaliveInterval) {
        this.keepaliveInterval = keepaliveInterval;
    }

    public long getTransferSize() {
        return transferSize;
    }

    public void setTransferSize(long transferSize) {
        this.transferSize = transferSize;
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

    public TransferType getTransferType() {
        return transferType;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public String getDownloadFilename() {
        return downloadFilename;
    }

    public void setDownloadFilename(String downloadFilename) {
        this.downloadFilename = downloadFilename;
    }

    public boolean isRetryExtendTicket() {
        return retryExtendTicket;
    }

    public void setRetryExtendTicket(boolean retryExtendTicket) {
        this.retryExtendTicket = retryExtendTicket;
    }
}

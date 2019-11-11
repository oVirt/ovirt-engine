package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;

public class TransferImageStatusParameters extends ActionParametersBase {
    private static final long serialVersionUID = 8404863745376386682L;
    private Guid transferImageCommandId;
    private Guid diskId;
    private ImageTransfer updates;
    private AuditLogType auditLogType;
    private String proxyLocation;
    private Guid storageDomainId;

    public TransferImageStatusParameters(Guid transferImageCommandId, ImageTransfer updates) {
        this.transferImageCommandId = transferImageCommandId;
        this.updates = updates;
    }

    public TransferImageStatusParameters(Guid transferImageCommandId) {
        this.transferImageCommandId = transferImageCommandId;
    }

    public TransferImageStatusParameters() {
    }

    public Guid getTransferImageCommandId() {
        return transferImageCommandId;
    }

    public void setTransferImageCommandId(Guid transferImageCommandId) {
        this.transferImageCommandId = transferImageCommandId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }

    public ImageTransfer getUpdates() {
        return updates;
    }

    public void setUpdates(ImageTransfer updates) {
        this.updates = updates;
    }

    public void setAuditLogType(AuditLogType logType) {
        this.auditLogType = logType;
    }

    public AuditLogType getAuditLogType() {
        return auditLogType;
    }

    public String getProxyLocation() {
        return proxyLocation;
    }

    public void setProxyLocation(String proxyLocation) {
        this.proxyLocation = proxyLocation;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}

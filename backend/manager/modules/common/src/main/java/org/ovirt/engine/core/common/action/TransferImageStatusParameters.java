package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;

public class TransferImageStatusParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 8404863745376386682L;
    private Guid transferImageCommandId;
    private Guid diskId;
    private ImageTransfer updates;
    private AuditLogType auditLogType;

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
}

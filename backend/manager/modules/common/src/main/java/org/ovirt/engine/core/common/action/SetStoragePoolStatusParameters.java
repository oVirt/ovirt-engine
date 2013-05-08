package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;

public class SetStoragePoolStatusParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = 264321499194008199L;
    private StoragePoolStatus privateStatus = StoragePoolStatus.forValue(0);

    public StoragePoolStatus getStatus() {
        return privateStatus;
    }

    public void setStatus(StoragePoolStatus value) {
        privateStatus = value;
    }

    private AuditLogType privateAuditLogType = AuditLogType.forValue(0);

    public AuditLogType getAuditLogType() {
        return privateAuditLogType;
    }

    public void setAuditLogType(AuditLogType value) {
        privateAuditLogType = value;
    }

    private VdcBllErrors privateError = VdcBllErrors.forValue(0);

    public VdcBllErrors getError() {
        return privateError;
    }

    public void setError(VdcBllErrors value) {
        privateError = value;
    }

    public SetStoragePoolStatusParameters(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType) {
        super(storagePoolId);
        setStatus(status);
        setAuditLogType(auditLogType);
    }

    public SetStoragePoolStatusParameters() {
    }
}

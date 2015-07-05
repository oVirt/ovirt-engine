package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.compat.Guid;

public class SetStoragePoolStatusParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = 264321499194008199L;
    private StoragePoolStatus privateStatus;

    public StoragePoolStatus getStatus() {
        return privateStatus;
    }

    public void setStatus(StoragePoolStatus value) {
        privateStatus = value;
    }

    private AuditLogType privateAuditLogType;

    public AuditLogType getAuditLogType() {
        return privateAuditLogType;
    }

    public void setAuditLogType(AuditLogType value) {
        privateAuditLogType = value;
    }

    private EngineError privateError;

    public EngineError getError() {
        return privateError;
    }

    public void setError(EngineError value) {
        privateError = value;
    }

    public SetStoragePoolStatusParameters(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType) {
        super(storagePoolId);
        setStatus(status);
        setAuditLogType(auditLogType);
        privateError = EngineError.Done;
    }

    public SetStoragePoolStatusParameters() {
        privateStatus = StoragePoolStatus.Uninitialized;
        privateAuditLogType = AuditLogType.UNASSIGNED;
        privateError = EngineError.Done;
    }
}

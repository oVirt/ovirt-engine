package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolWithStoragesParameter extends StoragePoolManagementParameter {
    private static final long serialVersionUID = 399202796107792151L;
    private List<Guid> privateStorages;

    public List<Guid> getStorages() {
        return privateStorages;
    }

    private void setStorages(List<Guid> value) {
        privateStorages = value;
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    public void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public StoragePoolWithStoragesParameter(StoragePool storagePool, List<Guid> storage_domain_ids, String sessionId) {
        super(storagePool);
        setStorages(storage_domain_ids);
        setSessionId(sessionId);
    }

    public StoragePoolWithStoragesParameter() {
    }
}

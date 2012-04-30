package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class StoragePoolWithStoragesParameter extends StoragePoolManagementParameter {
    private static final long serialVersionUID = 399202796107792151L;
    private java.util.ArrayList<Guid> privateStorages;

    public java.util.ArrayList<Guid> getStorages() {
        return privateStorages;
    }

    private void setStorages(java.util.ArrayList<Guid> value) {
        privateStorages = value;
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    public void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public StoragePoolWithStoragesParameter(storage_pool storagePool, java.util.ArrayList<Guid> storage_domain_ids, String sessionId) {
        super(storagePool);
        setStorages(storage_domain_ids);
        setSessionId(sessionId);
    }

    public StoragePoolWithStoragesParameter() {
    }
}

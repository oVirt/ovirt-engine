package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public interface HasStoragePool {
    Guid getStoragePoolId();

    void setStoragePoolId(Guid value);
}

package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public interface HasStoragePool<T extends Serializable> {
    T getStoragePoolId();

    void setStoragePoolId(T value);
}

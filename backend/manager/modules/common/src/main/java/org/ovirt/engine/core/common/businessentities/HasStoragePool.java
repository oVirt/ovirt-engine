package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public interface HasStoragePool<T extends Serializable> {
    T getstorage_pool_id();

    void setstorage_pool_id(T value);
}

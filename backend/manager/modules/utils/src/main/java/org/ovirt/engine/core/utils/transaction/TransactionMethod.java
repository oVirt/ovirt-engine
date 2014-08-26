package org.ovirt.engine.core.utils.transaction;

public interface TransactionMethod<V> {
    V runInTransaction();
}

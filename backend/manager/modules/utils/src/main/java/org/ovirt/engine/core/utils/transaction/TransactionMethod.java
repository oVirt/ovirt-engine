package org.ovirt.engine.core.utils.transaction;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 30, 2009 Time: 1:32:50 PM To change this template use File |
 * Settings | File Templates.
 */
public interface TransactionMethod<V> {
    V runInTransaction();
}

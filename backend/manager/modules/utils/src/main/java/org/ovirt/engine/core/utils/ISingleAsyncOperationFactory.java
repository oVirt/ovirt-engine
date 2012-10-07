package org.ovirt.engine.core.utils;

public interface ISingleAsyncOperationFactory {
    void initialize(java.util.ArrayList parameters);

    ISingleAsyncOperation CreateSingleAsyncOperation();
}

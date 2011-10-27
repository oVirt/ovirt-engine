package org.ovirt.engine.core.utils;

public interface ISingleAsyncOperationFactory {
    void Initialize(java.util.ArrayList parameters);

    ISingleAsyncOperation CreateSingleAsyncOperation();
}

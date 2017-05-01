package org.ovirt.engine.core.utils;

import java.util.List;

public interface ISingleAsyncOperationFactory {
    void initialize(List<?> parameters);

    ISingleAsyncOperation createSingleAsyncOperation();
}

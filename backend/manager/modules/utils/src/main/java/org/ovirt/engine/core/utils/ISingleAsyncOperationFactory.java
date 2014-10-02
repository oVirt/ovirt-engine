package org.ovirt.engine.core.utils;

import java.util.ArrayList;

public interface ISingleAsyncOperationFactory {
    void initialize(ArrayList<?> parameters);

    ISingleAsyncOperation createSingleAsyncOperation();
}

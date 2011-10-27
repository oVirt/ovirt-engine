package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.*;

public class ConnectSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    @Override
    public ISingleAsyncOperation CreateSingleAsyncOperation() {
        return new ConnectSingleAsyncOperation(getVdss(), getStorageDomain(), getStoragePool());
    }
}

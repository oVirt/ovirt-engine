package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class ConnectSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new ConnectSingleAsyncOperation(getVdss(), getStorageDomain(), getStoragePool());
    }
}

package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class RefreshStoragePoolAndDisconnectAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new RefreshStoragePoolAndDisconnectAsyncOperation(getVdss(), getStorageDomain(), getStoragePool());
    }
}

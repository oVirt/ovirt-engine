package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.*;

public class RefreshStoragePoolAndDisconnectAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new RefreshStoragePoolAndDisconnectAsyncOperation(getVdss(), getStorageDomain(), getStoragePool());
    }
}

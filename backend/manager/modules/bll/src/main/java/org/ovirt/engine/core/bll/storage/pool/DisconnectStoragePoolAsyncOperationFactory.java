package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class DisconnectStoragePoolAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new DisconnectStoragePoolAsyncOperation(getVdss(), getStoragePool());
    }
}

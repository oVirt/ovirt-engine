package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.utils.*;

public class DisconnectStoragePoolAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new DisconnectStoragePoolAsyncOperation(getVdss(), getStoragePool());
    }
}

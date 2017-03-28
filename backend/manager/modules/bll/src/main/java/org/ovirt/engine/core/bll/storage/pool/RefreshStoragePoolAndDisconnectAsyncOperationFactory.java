package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class RefreshStoragePoolAndDisconnectAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return Injector.injectMembers(
                new RefreshStoragePoolAndDisconnectAsyncOperation(getVdss(), getStorageDomain(), getStoragePool()));
    }
}

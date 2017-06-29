package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class AfterDeactivateSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    private boolean isLastMaster;
    private Guid newMasterStorageDomainId = Guid.Empty;

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return Injector.injectMembers(
                new AfterDeactivateSingleAsyncOperation(getVdss(),
                        getStorageDomain(),
                        getStoragePool(),
                        isLastMaster,
                        newMasterStorageDomainId));
    }

    @Override
    public void initialize(List parameters) {
        super.initialize(parameters);
        if (!(parameters.get(3) instanceof Boolean)) {
            throw new IllegalArgumentException();
        }
        isLastMaster = (Boolean) parameters.get(3);
        if (!(parameters.get(4) instanceof Guid)) {
            throw new IllegalArgumentException();
        }
        newMasterStorageDomainId = (Guid) parameters.get(4);
    }
}

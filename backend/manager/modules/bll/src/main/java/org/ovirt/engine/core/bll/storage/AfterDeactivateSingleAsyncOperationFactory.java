package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class AfterDeactivateSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    private boolean _isLastMaster;
    private Guid _newMasterStorageDomainId = new Guid();

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return new AfterDeactivateSingleAsyncOperation(getVdss(), getStorageDomain(), getStoragePool(), _isLastMaster,
                _newMasterStorageDomainId);
    }

    @Override
    public void initialize(java.util.ArrayList parameters) {
        super.initialize(parameters);
        if (!(parameters.get(3) instanceof Boolean)) {
            throw new InvalidOperationException();
        }
        _isLastMaster = (Boolean) (parameters.get(3));
        if (!(parameters.get(4) instanceof Guid)) {
            throw new InvalidOperationException();
        }
        _newMasterStorageDomainId = (Guid) parameters.get(4);
    }
}

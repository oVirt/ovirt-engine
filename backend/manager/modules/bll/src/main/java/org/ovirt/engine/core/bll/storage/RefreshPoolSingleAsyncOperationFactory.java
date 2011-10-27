package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.utils.*;

public class RefreshPoolSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    private java.util.ArrayList<Guid> _vdsIdsToSetNonOperational;

    @Override
    public void Initialize(java.util.ArrayList parameters) {
        super.Initialize(parameters);
        if (!(parameters.get(3) instanceof java.util.ArrayList)) {
            throw new InvalidOperationException();
        }
        java.util.ArrayList l = (java.util.ArrayList) parameters.get(3);
        if (!l.isEmpty() && !(l.get(0) instanceof Integer)) {
            throw new InvalidOperationException();
        }
        _vdsIdsToSetNonOperational = (java.util.ArrayList<Guid>) parameters.get(3);
    }

    @Override
    public ISingleAsyncOperation CreateSingleAsyncOperation() {
        RefObject<java.util.ArrayList<Guid>> tempRefObject = new RefObject<java.util.ArrayList<Guid>>(
                _vdsIdsToSetNonOperational);
        ISingleAsyncOperation tempVar = new RefreshPoolSingleAsyncOperation(getVdss(), getStorageDomain(),
                getStoragePool(), tempRefObject);
        _vdsIdsToSetNonOperational = tempRefObject.argvalue;
        return tempVar;
    }
}

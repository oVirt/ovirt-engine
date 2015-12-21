package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class RefreshPoolSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    private ArrayList<Guid> vdsIdsToSetNonOperational;

    @Override
    public void initialize(ArrayList parameters) {
        super.initialize(parameters);
        if (!(parameters.get(3) instanceof ArrayList)) {
            throw new IllegalArgumentException();
        }
        ArrayList l = (ArrayList) parameters.get(3);
        if (!l.isEmpty() && !(l.get(0) instanceof Integer)) {
            throw new IllegalArgumentException();
        }
        vdsIdsToSetNonOperational = (ArrayList<Guid>) parameters.get(3);
    }

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        ISingleAsyncOperation tempVar = new RefreshPoolSingleAsyncOperation(getVdss(), getStorageDomain(),
                getStoragePool(), vdsIdsToSetNonOperational);
        return tempVar;
    }
}

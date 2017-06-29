package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;

public class RefreshPoolSingleAsyncOperationFactory extends ActivateDeactivateSingleAsyncOperationFactory {
    private List<Guid> vdsIdsToSetNonOperational;

    @Override
    public void initialize(List parameters) {
        super.initialize(parameters);
        if (!(parameters.get(3) instanceof List)) {
            throw new IllegalArgumentException();
        }
        List l = (List) parameters.get(3);
        if (!l.isEmpty() && !(l.get(0) instanceof Integer)) {
            throw new IllegalArgumentException();
        }
        vdsIdsToSetNonOperational = (ArrayList<Guid>) parameters.get(3);
    }

    @Override
    public ISingleAsyncOperation createSingleAsyncOperation() {
        return Injector.injectMembers(
                new RefreshPoolSingleAsyncOperation(getVdss(), getStorageDomain(), getStoragePool(), vdsIdsToSetNonOperational));
    }
}

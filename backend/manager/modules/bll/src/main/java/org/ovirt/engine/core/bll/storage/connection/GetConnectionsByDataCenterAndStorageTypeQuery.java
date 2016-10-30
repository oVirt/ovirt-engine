package org.ovirt.engine.core.bll.storage.connection;

import java.util.EnumSet;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.queries.GetConnectionsByDataCenterAndStorageTypeParameters;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class GetConnectionsByDataCenterAndStorageTypeQuery<P extends GetConnectionsByDataCenterAndStorageTypeParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public GetConnectionsByDataCenterAndStorageTypeQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                storageServerConnectionDao
                        .getStorageConnectionsByStorageTypeAndStatus(getParameters().getId(),
                                getParameters().getStorageType(),
                                EnumSet.allOf(StorageDomainStatus.class)));
    }
}

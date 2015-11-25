package org.ovirt.engine.core.bll.storage.connection;

import java.util.EnumSet;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.queries.GetConnectionsByDataCenterAndStorageTypeParameters;

public class GetConnectionsByDataCenterAndStorageTypeQuery<P extends GetConnectionsByDataCenterAndStorageTypeParameters>
        extends QueriesCommandBase<P> {

    public GetConnectionsByDataCenterAndStorageTypeQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getStorageServerConnectionDao()
                        .getStorageConnectionsByStorageTypeAndStatus(getParameters().getId(),
                                getParameters().getStorageType(),
                                EnumSet.allOf(StorageDomainStatus.class)));
    }
}

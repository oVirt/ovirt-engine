package org.ovirt.engine.core.bll.storage.disk;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisksForVmQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllAttachableDisksForVmQuery<P extends GetAllAttachableDisksForVmQueryParameters> extends QueriesCommandBase<P> {

    public GetAllAttachableDisksForVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(DbFacade.getInstance()
                .getDiskDao()
                .getAllAttachableDisksByPoolId(getParameters().getStoragePoolId(),
                        getParameters().getVmId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}

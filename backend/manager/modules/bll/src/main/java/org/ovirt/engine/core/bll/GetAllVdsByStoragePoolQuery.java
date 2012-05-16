package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllVdsByStoragePoolParameters;

/** A query to return all the hosts in a given data center. */
public class GetAllVdsByStoragePoolQuery<P extends GetAllVdsByStoragePoolParameters> extends QueriesCommandBase<P> {
    public GetAllVdsByStoragePoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVdsDAO().getAllForStoragePool
                        (getParameters().getStoragePoolId(), getUserID(), getParameters().isFiltered()));
    }
}

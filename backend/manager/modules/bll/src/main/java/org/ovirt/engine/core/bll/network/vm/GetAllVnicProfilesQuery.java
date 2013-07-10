package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVnicProfilesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVnicProfilesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVnicProfileViewDao().getAll(getUserID(),
                getParameters().isFiltered()));
    }
}

package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllProvidersQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllProvidersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getProviderDao().getAll());
    }
}

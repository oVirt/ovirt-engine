package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllHostNamesPinnedToVmByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllHostNamesPinnedToVmByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVdsStaticDao()
                .getAllHostNamesPinnedToVm(getParameters().getId()));
    }

}

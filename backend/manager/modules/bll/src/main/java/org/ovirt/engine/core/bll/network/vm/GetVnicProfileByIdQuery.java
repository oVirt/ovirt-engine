package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVnicProfileByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVnicProfileByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVnicProfileViewDao().get(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}

package org.ovirt.engine.core.bll.network.vm;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmsByVnicProfileIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmsByVnicProfileIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVmDao().getAllForVnicProfile(getParameters().getId()));
    }
}

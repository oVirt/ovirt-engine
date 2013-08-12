package org.ovirt.engine.core.bll;


import org.ovirt.engine.core.common.queries.IdQueryParameters;


public class GetVnicProfilesByNetworkQosIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVnicProfilesByNetworkQosIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVnicProfileViewDao().getAllForNetworkQos(getParameters().getId()));
    }
}

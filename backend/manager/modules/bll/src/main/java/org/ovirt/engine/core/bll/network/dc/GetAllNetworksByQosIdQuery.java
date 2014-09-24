package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllNetworksByQosIdQuery extends QueriesCommandBase<IdQueryParameters> {

    public GetAllNetworksByQosIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDao().getAllForQos(getParameters().getId()));
    }

}

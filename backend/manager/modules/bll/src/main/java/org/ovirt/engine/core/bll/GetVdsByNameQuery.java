package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByNameParameters;

public class GetVdsByNameQuery<P extends GetVdsByNameParameters> extends QueriesCommandBase<P> {
    public GetVdsByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds = getDbFacade().getVdsDao().getByName(getParameters().getName());

        getQueryReturnValue().setReturnValue(vds);

    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByNameParameters;

public class GetVdsByNameQuery<P extends GetVdsByNameParameters> extends QueriesCommandBase<P> {
    public GetVdsByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> vds = getDbFacade().getVdsDAO().getAllWithName(getParameters().getName());

        if (vds.size() > 0) {
            getQueryReturnValue().setReturnValue(vds.get(0));
        }

    }
}

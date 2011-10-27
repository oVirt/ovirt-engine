package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVdsByNameQuery<P extends GetVdsByNameParameters> extends QueriesCommandBase<P> {
    public GetVdsByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> vds = DbFacade.getInstance().getVdsDAO().getAllWithName(getParameters().getName());

        if (vds.size() > 0) {
            getQueryReturnValue().setReturnValue(vds.get(0));
        }

    }
}

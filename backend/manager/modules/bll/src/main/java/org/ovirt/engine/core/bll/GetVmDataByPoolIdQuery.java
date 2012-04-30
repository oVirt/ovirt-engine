package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetVmDataByPoolIdQuery<P extends GetVmdataByPoolIdParameters> extends QueriesCommandBase<P> {

    public GetVmDataByPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVmPoolDAO()
                .getVmDataFromPoolByPoolGuid(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}

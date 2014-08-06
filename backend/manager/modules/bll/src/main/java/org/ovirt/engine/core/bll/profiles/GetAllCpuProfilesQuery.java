package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllCpuProfilesQuery extends QueriesCommandBase<VdcQueryParametersBase> {

    public GetAllCpuProfilesQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getCpuProfileDao().getAll());
    }

}

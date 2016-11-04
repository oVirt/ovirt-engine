package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmsRunningOnOrMigratingToVdsQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmsRunningOnOrMigratingToVdsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> allRunningOnOrMigratingToVds = getDbFacade().getVmDao().getAllRunningOnOrMigratingToVds(getParameters().getId());
        updateStatistics(allRunningOnOrMigratingToVds);

        getQueryReturnValue().setReturnValue(
                allRunningOnOrMigratingToVds);
    }

    protected void updateStatistics(List<VM> allRunningOnOrMigratingToVds) {
        if (allRunningOnOrMigratingToVds != null) {
            allRunningOnOrMigratingToVds.forEach(VmHandler::updateVmStatistics);
        }
    }

}

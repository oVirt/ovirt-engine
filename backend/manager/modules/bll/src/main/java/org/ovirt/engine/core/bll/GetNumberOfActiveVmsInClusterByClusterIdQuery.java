package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetNumberOfActiveVmsInClusterByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetNumberOfActiveVmsInClusterByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = DbFacade.getInstance().getVmDao().getAllForCluster(getParameters().getId());

        // Active VMs are VMs that aren't in Down status
        int activeVms = 0;
        for (VM vm : vms) {
            if (vm.getStatus() != VMStatus.Down && vm.getStatus() != VMStatus.ImageLocked) {
                ++activeVms;
            }
        }
        getQueryReturnValue().setReturnValue(activeVms);
    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetNumberOfActiveVmsInClusterByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    public GetNumberOfActiveVmsInClusterByClusterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = vmDao.getAllForCluster(getParameters().getId());

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

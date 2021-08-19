package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

/**
 * A query to retrieve all VM-Network Interface pairs that the given Network is attached to.
 */
public class GetVmsAndNetworkInterfacesByNetworkIdQuery<P extends GetVmsAndNetworkInterfacesByNetworkIdParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public GetVmsAndNetworkInterfacesByNetworkIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmList = vmDao.getAllForNetwork(getParameters().getId());
        List<VmNetworkInterface> vmNetworkInterfaceList =
                vmNetworkInterfaceDao.getAllForNetwork(getParameters().getId());

        final Map<Guid, VM> vmsById = Entities.businessEntitiesById(vmList);

        List<PairQueryable<VmNetworkInterface, VM>> vmInterfaceVmPairs = new ArrayList<>();
        for (VmNetworkInterface vmNetworkInterface : vmNetworkInterfaceList) {
            VM vm = vmsById.get(vmNetworkInterface.getVmId());
            vmHandler.updateConfiguredCpuVerb(vm);
            if (getParameters().getRunningVms() == null || getParameters().getRunningVms().equals(vm.isRunning())) {
                vmInterfaceVmPairs.add(new PairQueryable<>(vmNetworkInterface, vm));
            }
        }

        getQueryReturnValue().setReturnValue(vmInterfaceVmPairs);
    }
}

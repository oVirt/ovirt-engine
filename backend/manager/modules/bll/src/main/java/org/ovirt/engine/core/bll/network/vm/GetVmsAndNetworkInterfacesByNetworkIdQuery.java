package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmsAndNetworkInterfacesByNetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * A query to retrieve all VM-Network Interface pairs that the given Network is attached to.
 */
public class GetVmsAndNetworkInterfacesByNetworkIdQuery<P extends GetVmsAndNetworkInterfacesByNetworkIdParameters>
        extends QueriesCommandBase<P> {
    public GetVmsAndNetworkInterfacesByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmList = getDbFacade().getVmDao()
                .getAllForNetwork(getParameters().getId());
        List<VmNetworkInterface> vmNetworkInterfaceList = getDbFacade().getVmNetworkInterfaceDao()
                .getAllForNetwork(getParameters().getId());

        final Map<Guid, VM> vmsById = Entities.businessEntitiesById(vmList);

        List<PairQueryable<VmNetworkInterface, VM>> vmInterfaceVmPairs = new ArrayList<>();
        for (VmNetworkInterface vmNetworkInterface : vmNetworkInterfaceList) {
            VM vm = vmsById.get(vmNetworkInterface.getVmId());
            if (getParameters().getRunningVms() == null || getParameters().getRunningVms().equals(vm.isRunning())) {
                vmInterfaceVmPairs.add(new PairQueryable<>(vmNetworkInterface, vm));
            }
        }

        getQueryReturnValue().setReturnValue(vmInterfaceVmPairs);
    }
}

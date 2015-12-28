package org.ovirt.engine.core.bll.numa.vm;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class GetAllVmsWithNumaByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllVmsWithNumaByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid clusterId = getParameters().getId();
        List<VM> vms = getDbFacade().getVmDao().getAllForCluster(clusterId);
        List<Pair<Guid, VmNumaNode>> nodes = getDbFacade().getVmNumaNodeDao().getVmNumaNodeInfoByClusterId(clusterId);
        for (VM vm : vms) {
            for (Pair<Guid, VmNumaNode> pairnode : nodes) {
                if (vm.getId().equals(pairnode.getFirst())) {
                    vm.getvNumaNodeList().add(pairnode.getSecond());
                }
            }
        }
        getQueryReturnValue().setReturnValue(vms);
    }

}

package org.ovirt.engine.core.bll.numa.vm;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class GetAllVmsWithNumaByVdsGroupIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllVmsWithNumaByVdsGroupIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsGroupId = getParameters().getId();
        List<VM> vms = getDbFacade().getVmDao().getAllForVdsGroup(vdsGroupId);
        List<Pair<Guid, VmNumaNode>> nodes = getDbFacade().getVmNumaNodeDao().getVmNumaNodeInfoByVdsGroupId(vdsGroupId);
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

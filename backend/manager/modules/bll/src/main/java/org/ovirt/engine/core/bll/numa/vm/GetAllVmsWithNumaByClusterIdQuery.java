package org.ovirt.engine.core.bll.numa.vm;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public class GetAllVmsWithNumaByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    public GetAllVmsWithNumaByClusterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid clusterId = getParameters().getId();
        List<VM> vms = vmDao.getAllForCluster(clusterId);
        Map<Guid, List<VmNumaNode>> nodes = vmNumaNodeDao.getVmNumaNodeInfoByClusterId(clusterId);
        for (VM vm : vms) {
            if (nodes.containsKey(vm.getId())) {
                vm.setvNumaNodeList(nodes.get(vm.getId()));
            }
        }
        getQueryReturnValue().setReturnValue(vms);
    }

}

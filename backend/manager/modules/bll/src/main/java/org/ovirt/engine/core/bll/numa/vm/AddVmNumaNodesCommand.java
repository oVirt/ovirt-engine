package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class AddVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    public AddVmNumaNodesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        Guid vmId = getParameters().getVmId();
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        Guid vdsId = getVm().getDedicatedVmForVds();
        List<VdsNumaNode> vdsNumaNodes = new ArrayList<>();
        if (vdsId != null) {
            vdsNumaNodes = getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vdsId);
        }

        List<VdsNumaNode> nodes = new ArrayList<>();
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                int index = pair.getSecond().getSecond();
                for (VdsNumaNode vdsNumaNode : vdsNumaNodes) {
                    if (vdsNumaNode.getIndex() == index) {
                        pair.setFirst(vdsNumaNode.getId());
                        pair.getSecond().setFirst(true);
                        break;
                    }
                }
            }
            nodes.add(vmNumaNode);
        }
        getVmNumaNodeDao().massSaveNumaNode(nodes, null, vmId);

        // Used for restful API for reture first NUMA node GUID
        setActionReturnValue(nodes.get(0).getId());

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NUMA_ADD_VM_NUMA_NODE_SUCCESS
                : AuditLogType.NUMA_ADD_VM_NUMA_NODE_FAILED;
    }
}

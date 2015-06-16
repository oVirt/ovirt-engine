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

public class UpdateVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    public UpdateVmNumaNodesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        Guid vdsId = getVm().getDedicatedVmForVds();
        List<VdsNumaNode> vdsNumaNodes = new ArrayList<>();
        if (vdsId != null) {
            vdsNumaNodes = getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vdsId);
        }

        List<VdsNumaNode> nodes = new ArrayList<>();
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() != null && pair.getSecond().getFirst()) {
                    int index = pair.getSecond().getSecond();
                    for (VdsNumaNode vdsNumaNode : vdsNumaNodes) {
                        if (vdsNumaNode.getIndex() == index) {
                            pair.setFirst(vdsNumaNode.getId());
                            break;
                        }
                    }
                }
            }
            nodes.add(vmNumaNode);
        }
        getVmNumaNodeDao().massUpdateNumaNode(nodes);

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
        return getSucceeded() ? AuditLogType.NUMA_UPDATE_VM_NUMA_NODE_SUCCESS
                : AuditLogType.NUMA_UPDATE_VM_NUMA_NODE_FAILED;
    }
}

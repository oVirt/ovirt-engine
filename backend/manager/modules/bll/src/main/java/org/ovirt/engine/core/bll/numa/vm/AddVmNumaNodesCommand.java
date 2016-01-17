package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class AddVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    public AddVmNumaNodesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void doInit() {
        // Add new numa nodes to VM for inegrity checks
        getVmNumaNodesForValidation().addAll(getParameters().getVmNumaNodeList());
    }

    @Override
    protected void executeCommand() {
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        List<VdsNumaNode> vdsNumaNodes = getVdsNumaNodes();

        List<VmNumaNode> nodes = new ArrayList<>();
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            vmNumaNode.setId(Guid.newGuid());
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() != null && pair.getSecond().getSecond() != null) {
                    int index = pair.getSecond().getSecond();
                    // if pinned set pNode
                    if (pair.getSecond().getFirst()) {
                        for (VdsNumaNode vdsNumaNode : vdsNumaNodes) {
                            if (vdsNumaNode.getIndex() == index) {
                                pair.setFirst(vdsNumaNode.getId());
                                break;
                            }
                        }
                    }
                }
            }
            nodes.add(vmNumaNode);
        }
        getVmNumaNodeDao().massSaveNumaNode(nodes, null, getVm().getId());

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

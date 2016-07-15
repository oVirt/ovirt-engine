package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class SetVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    private List<Guid> oldNumaNodeIds;

    public SetVmNumaNodesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void doInit() {
        // Store old numa node ids for deletion
        oldNumaNodeIds = getVmNumaNodesForValidation().stream().map(VmNumaNode::getId).collect(Collectors.toList());
        // Set new numa nodes for validation
        setVmNumaNodesForValidation(getParameters().getVmNumaNodeList());
    }

    @Override
    protected void executeCommand() {
        if (!oldNumaNodeIds.isEmpty()) {
            vmNumaNodeDao.massRemoveNumaNodeByNumaNodeId(oldNumaNodeIds);
        }

        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        vmNumaNodes.stream()
                .forEach(node -> node.setId(Guid.newGuid()));

        vmNumaNodeDao.massSaveNumaNode(vmNumaNodes, getVm().getId());

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

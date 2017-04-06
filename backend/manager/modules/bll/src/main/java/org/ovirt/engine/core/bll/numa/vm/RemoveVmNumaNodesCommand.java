package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public class RemoveVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    public RemoveVmNumaNodesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void doInit() {
        // remove numa nodes to delete for checks
        final Map<Guid, VmNumaNode> removedNodeMap = new HashMap<>();
        for (VmNumaNode numaNode : getParameters().getVmNumaNodeList()) {
            removedNodeMap.put(numaNode.getId(), numaNode);
        }
        for (ListIterator<VmNumaNode> iterator = getVmNumaNodesForValidation().listIterator(); iterator.hasNext(); ) {
            final VmNumaNode updatedNode = removedNodeMap.get(iterator.next().getId());
            if (updatedNode != null) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        try {
            List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
            List<Guid> guids = new ArrayList<>();
            for (VmNumaNode node : vmNumaNodes) {
                guids.add(node.getId());
            }
            vmNumaNodeDao.massRemoveNumaNodeByNumaNodeId(guids);
            succeeded = true;
        } finally {
            setSucceeded(succeeded);
        }
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
        return getSucceeded() ? AuditLogType.NUMA_REMOVE_VM_NUMA_NODE_SUCCESS
                : AuditLogType.NUMA_REMOVE_VM_NUMA_NODE_FAILED;
    }
}

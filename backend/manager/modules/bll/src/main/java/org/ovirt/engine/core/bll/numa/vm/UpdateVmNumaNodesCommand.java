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

public class UpdateVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends AbstractVmNumaNodeCommand<T> {

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;

    public UpdateVmNumaNodesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void doInit() {
        // replace existing numa nodes with updated numa nodes for checks
        final Map<Guid, VmNumaNode> updatedNodeMap = new HashMap<>();
        for (VmNumaNode numaNode : getParameters().getVmNumaNodeList()) {
            updatedNodeMap.put(numaNode.getId(), numaNode);
        }
        for (ListIterator<VmNumaNode> iterator = getVmNumaNodesForValidation().listIterator(); iterator.hasNext(); ) {
            final VmNumaNode updatedNode = updatedNodeMap.get(iterator.next().getId());
            if (updatedNode != null) {
                iterator.set(updatedNode);
            }
        }
    }

    @Override
    protected void executeCommand() {
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        vmNumaNodes.forEach(node -> node.setNumaTuneMode(getEffectiveNumaTune(node)));
        vmNumaNodeDao.massUpdateNumaNode(vmNumaNodes);
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

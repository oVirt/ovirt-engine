package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class RemoveVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends VmCommand<T> {

    public RemoveVmNumaNodesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        try {
            List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
            List<Guid> guids = new ArrayList<Guid>();
            for (VmNumaNode node : vmNumaNodes) {
                guids.add(node.getId());
            }
            getDbFacade().getVmNumaNodeDAO().massRemoveNumaNodeByNumaNodeId(guids);
            succeeded = true;
        } finally {
            setSucceeded(succeeded);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
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

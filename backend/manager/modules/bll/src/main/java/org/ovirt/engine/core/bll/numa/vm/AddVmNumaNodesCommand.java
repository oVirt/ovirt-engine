package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class AddVmNumaNodesCommand<T extends VmNumaNodeOperationParameters> extends VmCommand<T> {

    public AddVmNumaNodesCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        boolean succeeded = false;
        try {
            Guid vmId = getParameters().getVmId();
            Guid vdsId = getVm().getDedicatedVmForVds();
            List<VdsNumaNode> vdsNumaNodes = new ArrayList<VdsNumaNode>();
            if (vdsId != null) {
                vdsNumaNodes = getDbFacade().getVdsNumaNodeDAO().getAllVdsNumaNodeByVdsId(vdsId);
            }
            List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
            List<VdsNumaNode> nodes = new ArrayList<VdsNumaNode>();
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
                nodes.add((VdsNumaNode) vmNumaNode);
            }
            getDbFacade().getVmNumaNodeDAO().massSaveNumaNode(nodes, null, vmId);
            // Used for restful API for reture first NUMA node GUID
            setActionReturnValue(nodes.get(0).getId());
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
        return getSucceeded() ? AuditLogType.NUMA_ADD_VM_NUMA_NODE_SUCCESS
                : AuditLogType.NUMA_ADD_VM_NUMA_NODE_FAILED;
    }
}

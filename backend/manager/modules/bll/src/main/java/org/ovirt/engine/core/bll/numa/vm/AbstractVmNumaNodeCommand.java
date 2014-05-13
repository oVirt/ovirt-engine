package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDAO;
import org.ovirt.engine.core.dao.VmNumaNodeDAO;

public abstract class AbstractVmNumaNodeCommand<T extends VmNumaNodeOperationParameters> extends VmCommand<T> {

    public AbstractVmNumaNodeCommand(T parameters) {
        super(parameters);
    }

    protected VdsNumaNodeDAO getVdsNumaNodeDao() {
        return getDbFacade().getVdsNumaNodeDAO();
    }

    protected VmNumaNodeDAO getVmNumaNodeDao() {
        return getDbFacade().getVmNumaNodeDAO();
    }

    @Override
    protected boolean canDoAction() {
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        if (vmNumaNodes == null || vmNumaNodes.size() == 0) {
            // if VM do not contain any NUMA node, skip checking
            return true;
        }

        VM vm = getVm();
        boolean pinHost = !Config.<Boolean> getValue(ConfigValues.SupportNUMAMigration);
        Guid vdsId = vm.getDedicatedVmForVds();
        if (pinHost && vdsId == null) {
            return failCanDoAction(VdcBllMessages.VM_NUMA_PINNED_VDS_NOT_EXIST);
        }

        List<VdsNumaNode> hostNumaNodes = new ArrayList<>();
        if (pinHost) {
            hostNumaNodes = getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vdsId);
            if (hostNumaNodes == null || hostNumaNodes.size() == 0) {
                return failCanDoAction(VdcBllMessages.VM_NUMA_PINNED_VDS_NODE_EMPTY);
            }
        }

        boolean memStrict = vm.getNumaTuneMode() == NumaTuneMode.STRICT;
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() == null || pair.getSecond().getSecond() == null) {
                    return failCanDoAction(VdcBllMessages.VM_NUMA_NODE_PINNED_INDEX_ERROR);
                }

                Integer index = pair.getSecond().getSecond();
                for (VdsNumaNode vdsNumaNode : hostNumaNodes) {
                    if (vdsNumaNode.getIndex() == index) {
                        if (memStrict && vmNumaNode.getMemTotal() > vdsNumaNode.getMemTotal()) {
                            return failCanDoAction(VdcBllMessages.VM_NUMA_NODE_MEMRORY_ERROR);
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }
}

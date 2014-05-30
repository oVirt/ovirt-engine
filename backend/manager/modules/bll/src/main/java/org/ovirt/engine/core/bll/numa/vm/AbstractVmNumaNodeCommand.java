package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
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

    protected Guid getDedicatedHost() {
        Guid dedicatedHost = getParameters().getDedicatedHost();
        if (dedicatedHost != null) {
            return dedicatedHost;
        }
        return getVm().getDedicatedVmForVds();
    }

    protected NumaTuneMode getNumaTuneMode() {
        NumaTuneMode numaTuneMode = getParameters().getNumaTuneMode();
        if (numaTuneMode != null) {
            return numaTuneMode;
        }
        return getVm().getNumaTuneMode();
    }

    protected MigrationSupport getMigrationSupport() {
        MigrationSupport migrationSupport = getParameters().getMigrationSupport();
        if (migrationSupport != null) {
            return migrationSupport;
        }
        return getVm().getMigrationSupport();
    }

    @Override
    protected boolean canDoAction() {
        List<VmNumaNode> vmNumaNodes = getParameters().getVmNumaNodeList();
        if (vmNumaNodes == null || vmNumaNodes.size() == 0) {
            // if VM do not contain any NUMA node, skip checking
            return true;
        }

        boolean pinHost = !Config.<Boolean> getValue(ConfigValues.SupportNUMAMigration);
        Guid vdsId = getDedicatedHost();
        if (pinHost && vdsId == null && getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failCanDoAction(VdcBllMessages.VM_NUMA_PINNED_VDS_NOT_EXIST);
        }

        List<VdsNumaNode> hostNumaNodes = new ArrayList<>();
        if (pinHost) {
            hostNumaNodes = getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vdsId);
            if (hostNumaNodes == null || hostNumaNodes.size() == 0) {
                return failCanDoAction(VdcBllMessages.VM_NUMA_PINNED_VDS_NODE_EMPTY);
            }
        }

        boolean memStrict = getNumaTuneMode() == NumaTuneMode.STRICT;
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

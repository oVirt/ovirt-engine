package org.ovirt.engine.core.bll.numa.vm;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public abstract class AbstractVmNumaNodeCommand<T extends VmNumaNodeOperationParameters> extends VmCommand<T> {

    public AbstractVmNumaNodeCommand(T parameters) {
        super(parameters);
    }

    protected VdsNumaNodeDao getVdsNumaNodeDao() {
        return getDbFacade().getVdsNumaNodeDao();
    }

    protected VmNumaNodeDao getVmNumaNodeDao() {
        return getDbFacade().getVmNumaNodeDao();
    }

    protected List<Guid> getDedicatedHostList() {
        List<Guid> dedicatedHost = getParameters().getDedicatedHostList();
        if (!dedicatedHost.isEmpty()) {
            return dedicatedHost;
        }
        return getVm().getDedicatedVmForVdsList();
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

        if (!validate(VmHandler.checkNumaPreferredTuneMode(getParameters().getNumaTuneMode(), vmNumaNodes, getVmId()))) {
            return false;
        }

        if (vmNumaNodes == null || vmNumaNodes.size() == 0) {
            // if VM do not contain any NUMA node, skip checking
            return true;
        }

        List<VdsNumaNode> hostNumaNodes = new ArrayList<>();
        if (!Config.<Boolean> getValue(ConfigValues.SupportNUMAMigration)) {// if unsupported NUMA migration
            // validate - pinning is mandatory, since migration is not allowed
            if (getMigrationSupport() != MigrationSupport.PINNED_TO_HOST || getDedicatedHostList().size() == 0) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_PINNED_TO_HOST);
            }
            if (getDedicatedHostList().size() > 1) {
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_PINNED_TO_MULTIPLE_HOSTS);
            }
            hostNumaNodes = getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(getDedicatedHostList().get(0));
            if (hostNumaNodes == null || hostNumaNodes.isEmpty()) {
                return failCanDoAction(EngineMessage.VM_NUMA_PINNED_VDS_NODE_EMPTY);
            }
        }
        boolean memStrict = getNumaTuneMode() == NumaTuneMode.STRICT;
        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            for (Pair<Guid, Pair<Boolean, Integer>> pair : vmNumaNode.getVdsNumaNodeList()) {
                if (pair.getSecond() == null || pair.getSecond().getSecond() == null) {
                    return failCanDoAction(EngineMessage.VM_NUMA_NODE_PINNED_INDEX_ERROR);
                }

                Integer index = pair.getSecond().getSecond();
                for (VdsNumaNode vdsNumaNode : hostNumaNodes) {
                    if (vdsNumaNode.getIndex() == index) {
                        if (memStrict && vmNumaNode.getMemTotal() > vdsNumaNode.getMemTotal()) {
                            return failCanDoAction(EngineMessage.VM_NUMA_NODE_MEMRORY_ERROR);
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }
}

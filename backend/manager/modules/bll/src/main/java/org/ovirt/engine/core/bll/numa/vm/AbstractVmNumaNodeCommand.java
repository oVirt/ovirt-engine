package org.ovirt.engine.core.bll.numa.vm;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

public abstract class AbstractVmNumaNodeCommand<T extends VmNumaNodeOperationParameters> extends VmCommand<T> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    private List<VmNumaNode> vmNumaNodesForValidation;

    public AbstractVmNumaNodeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected VdsNumaNodeDao getVdsNumaNodeDao() {
        return getDbFacade().getVdsNumaNodeDao();
    }

    protected VmNumaNodeDao getVmNumaNodeDao() {
        return getDbFacade().getVmNumaNodeDao();
    }

    @Override
    protected final void init() {
        super.init();
        if (getParameters().getVmId() != null) {
            setVmId(getParameters().getVmId());

        } else {
            setVm(getParameters().getVm());
        }
        if (getVm() != null) {
            setVmNumaNodesForValidation(getVmNumaNodeDao().getAllVmNumaNodeByVmId(getVm().getId()));
            doInit();
        }
    }

    protected abstract void doInit();

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        if (!validate(NumaValidator.checkVmNumaIndexDuplicates(getParameters().getVmNumaNodeList()))) {
            return false;
        }
        return validate(NumaValidator.checkVmNumaNodesIntegrity(getVm(), getVmNumaNodesForValidation()));
    }

    protected List<VmNumaNode> getVmNumaNodesForValidation() {
        return vmNumaNodesForValidation;
    }

    protected void setVmNumaNodesForValidation(List<VmNumaNode> vmNumaNodesForValidation) {
        this.vmNumaNodesForValidation = vmNumaNodesForValidation;
    }

    protected List<VdsNumaNode> getVdsNumaNodes() {
        if (!getVm().getDedicatedVmForVdsList().isEmpty()) {
            Guid vdsId = getVm().getDedicatedVmForVdsList().get(0);
            if (vdsId != null) {
                return getVdsNumaNodeDao().getAllVdsNumaNodeByVdsId(vdsId);
            }
        }
        return Arrays.asList();
    }
}

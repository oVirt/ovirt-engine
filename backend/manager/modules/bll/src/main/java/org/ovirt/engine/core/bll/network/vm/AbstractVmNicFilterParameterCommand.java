package org.ovirt.engine.core.bll.network.vm;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.VmStaticDao;

public abstract class AbstractVmNicFilterParameterCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    @Inject
    private VmStaticDao vmStaticDao;


    public AbstractVmNicFilterParameterCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__NIC_FILTER_PARAMETER);
    }

    @Override
    protected void executeVmCommand() {
        setVmName(vmStaticDao.get(getParameters().getVmId()).getName());
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        return true;
    }

}

package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;

public class VmLogonCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public VmLogonCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Override
    protected void setActionMessageParameters () {
        addValidationMessage(EngineMessage.VAR__ACTION__LOGON);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        return true;
    }

    @Override
    protected void perform() {
        // Send the log on command to the virtual machine:
        final DbUser currentUser = getCurrentUser();
        final String password = sessionDataContainer.getPassword(getParameters().getSessionId());
        final String domainController = currentUser != null ? currentUser.getDomain() : "";
        final boolean sentToVM = runVdsCommand(
                        VDSCommandType.VmLogon,
                        new VmLogonVDSCommandParameters(
                                getVdsId(),
                                getVm().getId(),
                                domainController,
                                getUserName(),
                                password)).getSucceeded();
        setSucceeded(sentToVM);
    }
}

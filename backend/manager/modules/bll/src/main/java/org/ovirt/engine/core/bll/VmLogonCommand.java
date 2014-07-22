package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;

public class VmLogonCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public VmLogonCommand(T parameters) {
        super(parameters, null);
    }

    public VmLogonCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters () {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__LOGON);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        // Check that the virtual machine exists:
        final VM vm = getVm();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        // Everything is OK:
        return true;
    }

    @Override
    protected void perform() {
        // Get a reference to the virtual machine:
        final VM vm = getVm();

        // Send the log on command to the virtual machine:
        final DbUser currentUser = getCurrentUser();
        final String password = SessionDataContainer.getInstance().getPassword(getParameters().getSessionId());
        final String domainController = currentUser != null ? currentUser.getDomain() : "";
        final boolean sentToVM = runVdsCommand(
                        VDSCommandType.VmLogon,
                        new VmLogonVDSCommandParameters(getVdsId(), vm.getId(), domainController,
                                getUserName(), password)).getSucceeded();
        // Done:
        setSucceeded(sentToVM);
    }
}

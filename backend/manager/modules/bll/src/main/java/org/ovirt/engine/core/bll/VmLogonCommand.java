package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogonVDSCommandParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

@InternalCommandAttribute
public class VmLogonCommand<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {

    public VmLogonCommand(T parameters) {
        super(parameters);
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

        // Everything is OK:
        return true;
    }

    @Override
    protected void Perform() {
        // Get a reference to the virtual machine:
        final VM vm = getVm();

        // Send the log on command to the virtual machine:
        final IVdcUser currentUser = getCurrentUser();
        final String domainController = currentUser != null ? currentUser.getDomainControler() : "";
        final String password = currentUser != null ? currentUser.getPassword() : "";
        final boolean sentToVM = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.VmLogon,
                        new VmLogonVDSCommandParameters(getVdsId(), vm.getId(), domainController,
                                getUserName(), password)).getSucceeded();
        // Done:
        setSucceeded(sentToVM);
    }
}

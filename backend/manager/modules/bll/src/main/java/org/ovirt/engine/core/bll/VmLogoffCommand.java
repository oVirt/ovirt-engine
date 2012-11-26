package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.LogoffVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VmLogoffVDSCommandParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class VmLogoffCommand<T extends LogoffVmParameters> extends VmOperationCommandBase<T> {
    // Serialization id:
    private static final long serialVersionUID = 8862388238073799574L;

    private boolean mForce;

    public VmLogoffCommand(T parameters) {
        super(parameters);
        mForce = parameters.getForce();
    }

    protected void setActionMessageParameters () {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__LOGOFF);
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

        // Send the log off command to the virtual machine:
        final boolean sentToVM = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.VmLogoff,
                        new VmLogoffVDSCommandParameters(getVdsId(), vm.getId(), mForce)).getSucceeded();

        // If the command was sent to the virtual machine successfully update the
        // database to reflect that the user is logged off:
        if (sentToVM) {
            vm.setGuestCurUserName(null);
            vm.setGuestCurUserId(null);
            DbFacade.getInstance().getVmDynamicDao().update(vm.getDynamicData());
        }

        // Done:
        setSucceeded(sentToVM);
    }
}

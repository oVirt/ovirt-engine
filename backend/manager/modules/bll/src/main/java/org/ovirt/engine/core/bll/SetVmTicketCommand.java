package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.Ticketing;
import org.ovirt.engine.core.common.vdscommands.*;

@InternalCommandAttribute
public class SetVmTicketCommand<T extends SetVmTicketParameters> extends VmOperationCommandBase<T> {
    private String mTicket;
    private final int mValidTime;

    public SetVmTicketCommand(T parameters) {
        super(parameters);
        mTicket = parameters.getTicket();
        mValidTime = parameters.getValidTime();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        VM vm = DbFacade.getInstance().getVmDAO().getById(getVmId());
        if (vm == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else if (getVm().getstatus() != VMStatus.Up
                && getVm().getstatus() != VMStatus.Paused && getVm().getstatus() != VMStatus.PoweringUp
                && getVm().getstatus() != VMStatus.PoweringDown && getVm().getstatus() != VMStatus.RebootInProgress) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SET);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TICKET);
        }
        return returnValue;
    }

    @Override
    protected void Perform() {
        if (StringHelper.isNullOrEmpty(mTicket)) {
            // The ticket was not transfered by client we should generate it
            mTicket = Ticketing.GenerateOTP();
        }
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVmTicket,
                        new SetVmTicketVDSCommandParameters(getVdsId(), getVmId(), mTicket, mValidTime)).getSucceeded());

        if (getSucceeded()) {
            setActionReturnValue(mTicket);
            VM vm = DbFacade.getInstance().getVmDAO().getById(getVmId());
            vm.setguest_cur_user_name(getCurrentUser().getUserName());
            DbFacade.getInstance().getVmDynamicDAO().update(vm.getDynamicData());
        }
    }
}

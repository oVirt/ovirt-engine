package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.UpdateVmConsoleDataParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class UpdateVmConsoleDataCommand <T extends UpdateVmConsoleDataParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -3060640545994937332L;

    public UpdateVmConsoleDataCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        VM vm = getVm();
        vm.setConsoleCurrentUserName(getParameters().getConsoleUserName());

        VDSParametersBase updateVmDynamicParams =
                new UpdateVmDynamicDataVDSCommandParameters(getVdsId(), vm.getDynamicData());
        runVdsCommand(VDSCommandType.UpdateVmDynamicData, updateVmDynamicParams);

        setSucceeded(true);
    }

}

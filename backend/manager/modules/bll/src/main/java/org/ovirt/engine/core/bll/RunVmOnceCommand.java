package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> {
    public RunVmOnceCommand(T runVmParams) {
        super(runVmParams);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();

        // the condition allows to get only user and password which are both set (even with empty string) or both aren't
        // set (null), the action will fail if only one of those parameters is null.
        if (returnValue
                && (getParameters().getSysPrepUserName() == null ^ getParameters().getSysPrepPassword() == null)) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM);
            returnValue = false;
        }

        return returnValue;
    }

    @Override
    protected CreateVmVDSCommandParameters initVdsCreateVmParams() {
        getVm().setRunOnce(true);
        CreateVmVDSCommandParameters createVmParams = super.initVdsCreateVmParams();
        SysPrepParams sysPrepParams = new SysPrepParams();
        RunVmOnceParams runOnceParams = getParameters();
        sysPrepParams.setSysPrepDomainName(runOnceParams.getSysPrepDomainName());
        sysPrepParams.setSysPrepUserName(runOnceParams.getSysPrepUserName());
        sysPrepParams.setSysPrepPassword(runOnceParams.getSysPrepPassword());
        createVmParams.setSysPrepParams(sysPrepParams);
        return createVmParams;
    }

}

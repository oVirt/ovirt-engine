package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private VM vm;
    private SysPrepParams sysPrepParams;
    private boolean runInUnknownStatus;
    private InitializationType initializationType;

    public CreateVDSCommandParameters() {
    }

    public CreateVDSCommandParameters(Guid vdsId, VM vm) {
        super(vdsId, vm.getId());
        this.vm = vm;
    }

    public VM getVm() {
        return vm;
    }

    public SysPrepParams getSysPrepParams() {
        return sysPrepParams;
    }

    public void setSysPrepParams(SysPrepParams sysPrepParams) {
        this.sysPrepParams = sysPrepParams;
    }

    public boolean isRunInUnknownStatus() {
        return runInUnknownStatus;
    }

    public void setRunInUnknownStatus(boolean runUnknown) {
        this.runInUnknownStatus = runUnknown;
    }

    public InitializationType getInitializationType() {
        return initializationType;
    }

    public void setInitializationType(InitializationType value) {
        initializationType = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vm", getVm());
    }

}

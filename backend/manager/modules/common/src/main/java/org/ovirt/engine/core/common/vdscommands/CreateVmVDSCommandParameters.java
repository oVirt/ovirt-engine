package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class CreateVmVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public CreateVmVDSCommandParameters(Guid vdsId, VM vm) {
        super(vdsId, vm.getId());
        _vm = vm;
    }

    private VM _vm;

    private SysPrepParams sysPrepParams;

    public VM getVm() {
        return _vm;
    }

    public CreateVmVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vm=%s", super.toString(), getVm());
    }

    public SysPrepParams getSysPrepParams() {
        return sysPrepParams;
    }

    public void setSysPrepParams(SysPrepParams sysPrepParams) {
        this.sysPrepParams = sysPrepParams;
    }
}

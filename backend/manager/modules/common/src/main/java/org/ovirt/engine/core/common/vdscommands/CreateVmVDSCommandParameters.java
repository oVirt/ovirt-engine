package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateVmVDSCommandParameters")
public class CreateVmVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public CreateVmVDSCommandParameters(Guid vdsId, VM vm) {
        super(vdsId, vm.getvm_guid());
        _vm = vm;
    }

    @XmlElement
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

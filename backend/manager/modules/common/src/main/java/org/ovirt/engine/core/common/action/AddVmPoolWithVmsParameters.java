package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.vm_pools;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmPoolWithVmsParameters")
public class AddVmPoolWithVmsParameters extends VmPoolOperationParameters {
    private static final long serialVersionUID = 4826143612049185740L;

    @Valid
    @XmlElement
    private VM _vm;

    @XmlElement
    private int _vmsCount;

    @XmlElement
    private int _diskSize;

    public AddVmPoolWithVmsParameters(vm_pools vmPool, VM vm, int count, int diskSize) {
        super(vmPool);
        _vm = vm;
        _vmsCount = count;
        _diskSize = diskSize;
    }

    @Valid
    public VmStatic getVmStaticData() {
        return _vm.getStaticData();
    }

    public int getVmsCount() {
        return _vmsCount;
    }

    public int getDiskSize() {
        return _diskSize;
    }

    public AddVmPoolWithVmsParameters() {
    }
}

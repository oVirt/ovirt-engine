package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateVmDynamicDataVDSCommandParameters")
public class UpdateVmDynamicDataVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    @XmlElement(name = "VmDynamic")
    private VmDynamic privateVmDynamic;

    public VmDynamic getVmDynamic() {
        return privateVmDynamic;
    }

    private void setVmDynamic(VmDynamic value) {
        privateVmDynamic = value;
    }

    public UpdateVmDynamicDataVDSCommandParameters(Guid vdsId, VmDynamic vmDynamic) {
        super(vdsId);
        setVmDynamic(vmDynamic);
    }

    public UpdateVmDynamicDataVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmDynamic=%s", super.toString(), getVmDynamic());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsInfoVDSCommandParameters")
public class GetVmsInfoVDSCommandParameters extends StorageDomainIdParametersBase {

    @XmlElement(name = "VmIdList")
    private java.util.ArrayList<Guid> privateVmIdList;

    public java.util.ArrayList<Guid> getVmIdList() {
        return privateVmIdList;
    }

    public void setVmIdList(java.util.ArrayList<Guid> value) {
        privateVmIdList = value;
    }

    public GetVmsInfoVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetVmsInfoVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmIdList = %s", super.toString(), getVmIdList());
    }
}

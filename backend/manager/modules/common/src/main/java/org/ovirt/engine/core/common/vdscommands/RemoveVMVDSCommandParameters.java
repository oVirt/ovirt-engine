package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVMVDSCommandParameters")
public class RemoveVMVDSCommandParameters extends StorageDomainIdParametersBase {
    public RemoveVMVDSCommandParameters(Guid storagePoolId, Guid vmGuid) {
        this(storagePoolId, vmGuid, Guid.Empty);
    }

    public RemoveVMVDSCommandParameters(Guid storagePoolId, Guid vmGuid, Guid storageDomainId) {
        super(storagePoolId);
        setVmGuid(vmGuid);
        setStorageDomainId(storageDomainId);
    }

    @XmlElement(name = "VmGuid")
    private Guid privateVmGuid = new Guid();

    public Guid getVmGuid() {
        return privateVmGuid;
    }

    public void setVmGuid(Guid value) {
        privateVmGuid = value;
    }

    public RemoveVMVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmGuid = %s", super.toString(), getVmGuid());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ExtendStorageDomainVDSCommandParameters")
public class ExtendStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public ExtendStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            java.util.ArrayList<String> deviceList) {
        super(storagePoolId, storageDomainId);
        setDeviceList(deviceList);
    }

    @XmlElement(name = "DeviceList")
    private java.util.ArrayList<String> privateDeviceList;

    public java.util.ArrayList<String> getDeviceList() {
        return privateDeviceList;
    }

    private void setDeviceList(java.util.ArrayList<String> value) {
        privateDeviceList = value;
    }

    public ExtendStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, deviceList = %s", super.toString(), getDeviceList());
    }
}

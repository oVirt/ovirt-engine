package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateVGVDSCommandParameters")
public class CreateVGVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public CreateVGVDSCommandParameters(Guid vdsId, Guid storageDomainId, java.util.ArrayList<String> deviceList) {
        super(vdsId, storageDomainId);
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

    public CreateVGVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, deviceList=%s", super.toString(), getDeviceList());
    }
}

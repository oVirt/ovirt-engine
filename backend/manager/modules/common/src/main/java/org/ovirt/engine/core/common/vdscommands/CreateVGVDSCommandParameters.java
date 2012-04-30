package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class CreateVGVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public CreateVGVDSCommandParameters(Guid vdsId, Guid storageDomainId, java.util.ArrayList<String> deviceList) {
        super(vdsId, storageDomainId);
        setDeviceList(deviceList);
    }

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

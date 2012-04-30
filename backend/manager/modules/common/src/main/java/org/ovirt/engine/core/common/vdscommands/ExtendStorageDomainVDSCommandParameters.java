package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class ExtendStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public ExtendStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            java.util.ArrayList<String> deviceList) {
        super(storagePoolId, storageDomainId);
        setDeviceList(deviceList);
    }

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

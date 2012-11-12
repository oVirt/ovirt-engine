package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class ExtendStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public ExtendStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            java.util.ArrayList<String> deviceList, boolean force, boolean supportForceExtendVG) {
        super(storagePoolId, storageDomainId);
        setDeviceList(deviceList);
        setForce(force);
        setSupportForceExtendVG(supportForceExtendVG);
    }

    private java.util.ArrayList<String> privateDeviceList;

    public java.util.ArrayList<String> getDeviceList() {
        return privateDeviceList;
    }

    private void setDeviceList(java.util.ArrayList<String> value) {
        privateDeviceList = value;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    private boolean supportForceExtendVG;

    public boolean isSupportForceExtendVG() {
        return supportForceExtendVG;
    }

    public void setSupportForceExtendVG(boolean supportForceExtendVG) {
        this.supportForceExtendVG = supportForceExtendVG;
    }

    public ExtendStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, deviceList = %s", super.toString(), getDeviceList());
    }
}

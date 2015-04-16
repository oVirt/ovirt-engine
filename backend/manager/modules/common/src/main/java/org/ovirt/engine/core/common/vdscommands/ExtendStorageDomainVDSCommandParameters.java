package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ExtendStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public ExtendStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            ArrayList<String> deviceList, boolean force, boolean supportForceExtendVG) {
        super(storagePoolId, storageDomainId);
        setDeviceList(deviceList);
        setForce(force);
        setSupportForceExtendVG(supportForceExtendVG);
    }

    private ArrayList<String> privateDeviceList;

    public ArrayList<String> getDeviceList() {
        return privateDeviceList;
    }

    private void setDeviceList(ArrayList<String> value) {
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("deviceList", getDeviceList());
    }
}

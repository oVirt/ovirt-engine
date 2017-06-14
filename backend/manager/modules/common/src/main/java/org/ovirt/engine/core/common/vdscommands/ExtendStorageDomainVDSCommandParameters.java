package org.ovirt.engine.core.common.vdscommands;

import java.util.Set;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ExtendStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public ExtendStorageDomainVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId,
            Set<String> deviceList,
            boolean force) {
        super(storagePoolId, storageDomainId);
        setDeviceList(deviceList);
        setForce(force);
    }

    private Set<String> privateDeviceList;

    public Set<String> getDeviceList() {
        return privateDeviceList;
    }

    private void setDeviceList(Set<String> value) {
        privateDeviceList = value;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public ExtendStorageDomainVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("deviceList", getDeviceList());
    }
}

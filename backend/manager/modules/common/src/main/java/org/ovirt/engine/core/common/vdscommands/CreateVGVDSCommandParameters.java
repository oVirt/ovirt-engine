package org.ovirt.engine.core.common.vdscommands;

import java.util.Set;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateVGVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public CreateVGVDSCommandParameters(Guid vdsId, Guid storageDomainId, Set<String> deviceList, boolean force) {
        super(vdsId, storageDomainId);
        setDeviceList(deviceList);
        setForce(force);
    }

    private Set<String> deviceList;

    public Set<String> getDeviceList() {
        return deviceList;
    }

    private void setDeviceList(Set<String> value) {
        deviceList = value;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public CreateVGVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("deviceList", getDeviceList())
                .append("force", isForce());
    }
}

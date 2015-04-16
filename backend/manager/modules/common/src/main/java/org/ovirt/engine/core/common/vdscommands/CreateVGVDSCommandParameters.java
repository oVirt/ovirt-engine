package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateVGVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public CreateVGVDSCommandParameters(Guid vdsId, Guid storageDomainId, ArrayList<String> deviceList, boolean force) {
        super(vdsId, storageDomainId);
        setDeviceList(deviceList);
        setForce(force);
    }

    private List<String> privateDeviceList;

    public List<String> getDeviceList() {
        return privateDeviceList;
    }

    private void setDeviceList(List<String> value) {
        privateDeviceList = value;
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

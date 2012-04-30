package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class PowerClientMigrateOnConnectCheckParameters extends MigrateVmParameters {
    private static final long serialVersionUID = -7195616799472809959L;
    private String privateClientIp;

    public String getClientIp() {
        return privateClientIp;
    }

    private void setClientIp(String value) {
        privateClientIp = value;
    }

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public PowerClientMigrateOnConnectCheckParameters(boolean force, Guid vmId, String clientIp, Guid vdsId) {
        super(force, vmId);
        setClientIp(clientIp);
        setVdsId(vdsId);
    }

    public PowerClientMigrateOnConnectCheckParameters() {
    }
}

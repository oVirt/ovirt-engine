package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VDSGroupHostsAndVMs extends IVdcQueryable implements Serializable {

    private static final long serialVersionUID = -5395392502656683858L;

    private Guid vdsGroupId;
    private int hosts;
    private int vms;

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid vdsGroupId) {
        this.vdsGroupId = vdsGroupId;
    }

    public int getHosts() {
        return hosts;
    }

    public void setHosts(int hosts) {
        this.hosts = hosts;
    }

    public int getVms() {
        return vms;
    }

    public void setVms(int vms) {
        this.vms = vms;
    }

}

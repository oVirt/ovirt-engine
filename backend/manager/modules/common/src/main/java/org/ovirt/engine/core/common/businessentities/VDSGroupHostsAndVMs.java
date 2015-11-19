package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class VDSGroupHostsAndVMs implements IVdcQueryable {

    private static final long serialVersionUID = -5395392502656683858L;

    private Guid vdsGroupId;
    private int hosts;
    private int vms;

    @Override
    public Object getQueryableId() {
        return getVdsGroupId();
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(
                hosts,
                vdsGroupId,
                vms
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VDSGroupHostsAndVMs)) {
            return false;
        }
        VDSGroupHostsAndVMs other = (VDSGroupHostsAndVMs) obj;
        return hosts == other.hosts
                && Objects.equals(vdsGroupId, other.vdsGroupId)
                && vms == other.vms;
    }

}

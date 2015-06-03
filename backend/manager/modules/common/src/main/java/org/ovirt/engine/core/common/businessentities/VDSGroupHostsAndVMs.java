package org.ovirt.engine.core.common.businessentities;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + hosts;
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + vms;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VDSGroupHostsAndVMs other = (VDSGroupHostsAndVMs) obj;
        if (hosts != other.hosts) {
            return false;
        }
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null) {
                return false;
            }
        } else if (!vdsGroupId.equals(other.vdsGroupId)) {
            return false;
        }
        if (vms != other.vms) {
            return false;
        }
        return true;
    }

}

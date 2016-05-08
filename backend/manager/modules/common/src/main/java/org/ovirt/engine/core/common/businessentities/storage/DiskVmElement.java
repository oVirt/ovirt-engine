package org.ovirt.engine.core.common.businessentities.storage;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;

public class DiskVmElement implements BusinessEntity<VmDeviceId> {
    /**
     * The vm device id of the disk vm element, this will be consisted of the disk id along with the vm id
     */
    private VmDeviceId id;

    public DiskVmElement() {
    }

    public DiskVmElement(VmDeviceId id) {
        setId(id);
    }

    public DiskVmElement(Guid diskId, Guid vmId) {
        setId(new VmDeviceId(diskId, vmId));
    }

    public VmDeviceId getId() {
        return id;
    }

    public void setId(VmDeviceId id) {
        this.id = id;
    }

    public Guid getDiskId() {
        return getId().getDeviceId();
    }

    public Guid getVmId() {
        return getId().getVmId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiskVmElement that = (DiskVmElement) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

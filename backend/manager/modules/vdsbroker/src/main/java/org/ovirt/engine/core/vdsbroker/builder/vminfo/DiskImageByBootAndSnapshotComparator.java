package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;

final class DiskImageByBootAndSnapshotComparator implements Comparator<Disk>, Serializable {
    private static final long serialVersionUID = 4732164571328497830L;
    private Guid vmId;

    DiskImageByBootAndSnapshotComparator(Guid vmId) {
        this.vmId = vmId;
    }

    @Override
    public int compare(Disk o1, Disk o2) {
        Boolean boot1 = o1.getDiskVmElementForVm(vmId).isBoot();
        Boolean boot2 = o2.getDiskVmElementForVm(vmId).isBoot();
        int bootResult = boot1.compareTo(boot2);
        if (bootResult == 0 && boot1) {
            return Boolean.compare(o2.isDiskSnapshot(), o1.isDiskSnapshot());
        }
        return bootResult;
    }
}

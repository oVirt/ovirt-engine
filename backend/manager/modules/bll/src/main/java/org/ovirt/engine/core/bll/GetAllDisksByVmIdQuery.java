package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class GetAllDisksByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllDisksByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> allDisks =
                getDbFacade().getDiskDao().getAllForVm
                        (getParameters().getId(), getUserID(), getParameters().isFiltered());
        Set<Guid> pluggedDiskIds = getPluggedDiskIds();
        List<Disk> disks = new ArrayList<Disk>(allDisks);
        for (Disk disk : allDisks) {
            disk.setPlugged(pluggedDiskIds.contains(disk.getId()));
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(getAllImageSnapshots(diskImage));
            }
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId(), diskImage.getImageTemplateId());
    }

    private Set<Guid> getPluggedDiskIds() {
        List<VmDevice> disksVmDevices =
                getDbFacade().getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(getParameters().getId(),
                                VmDeviceGeneralType.DISK,
                                VmDeviceType.DISK.getName(),
                                getUserID(),
                                getParameters().isFiltered());
        Set<Guid> pluggedDiskIds = new HashSet<Guid>();
        for (VmDevice diskVmDevice : disksVmDevices) {
            if (diskVmDevice.getIsPlugged()) {
                pluggedDiskIds.add(diskVmDevice.getDeviceId());
            }
        }
        return pluggedDiskIds;
    }
}

package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.EngineContext;
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

    public GetAllDisksByVmIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> allDisks =
                getDbFacade().getDiskDao().getAllForVm
                        (getParameters().getId(), getUserID(), getParameters().isFiltered());
        Map<Guid, VmDevice> disksVmDevices = getDisksVmDeviceMap();
        List<Disk> disks = new ArrayList<Disk>();
        for (Disk disk : allDisks) {
            VmDevice diskDevice = disksVmDevices.get(disk.getId());
            if (diskDevice != null) {
                disk.setPlugged(diskDevice.getIsPlugged());
                disk.setReadOnly(diskDevice.getIsReadOnly());
                disk.setLogicalName(diskDevice.getLogicalName());
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    diskImage.getSnapshots().addAll(getAllImageSnapshots(diskImage));
                }
                disks.add(disk);
            }
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }

    private Map<Guid, VmDevice> getDisksVmDeviceMap() {
        List<VmDevice> disksVmDevices =
                getDbFacade().getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(getParameters().getId(),
                                VmDeviceGeneralType.DISK,
                                VmDeviceType.DISK.getName(),
                                getUserID(),
                                getParameters().isFiltered());

        if (disksVmDevices.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Guid, VmDevice> toReturn = new HashMap<>();
        for (VmDevice diskVmDevice : disksVmDevices) {
            toReturn.put(diskVmDevice.getDeviceId(), diskVmDevice);
        }
        return toReturn;
    }
}

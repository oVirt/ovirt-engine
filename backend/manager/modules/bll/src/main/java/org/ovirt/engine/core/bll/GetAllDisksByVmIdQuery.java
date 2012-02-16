package org.ovirt.engine.core.bll;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetAllDisksByVmIdQuery<P extends GetAllDisksByVmIdParameters> extends QueriesCommandBase<P> {
    public GetAllDisksByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // LINQ
        // DbFacade.Instance.GetImagesByVmGuid(GetParameters.VmId).Where(image=>image.active
        // == true).ToList();
        List<DiskImage> disks =
                LinqUtils.filter(
                        DbFacade.getInstance().getDiskImageDAO().getAllForVm(getParameters().getVmId()),
                        new Predicate<DiskImage>() {
                            @Override
                            public boolean eval(DiskImage diskImage) {
                                return (diskImage.getactive());
                            }
                        });
        Set<Guid> pluggedDiskIds = getPluggedDiskIds();
        for (DiskImage diskImage : disks) {
            diskImage.getSnapshots().addAll(
                    ImagesHandler.getAllImageSnapshots(diskImage.getId(), diskImage.getit_guid()));
            diskImage.setPlugged(pluggedDiskIds.contains(diskImage.getDisk().getId()));
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    private Set<Guid> getPluggedDiskIds() {
        List<VmDevice> disksVmDevices =
                DbFacade.getInstance().getVmDeviceDAO().getVmDeviceByVmIdTypeAndDevice(getParameters().getVmId(),
                        VmDeviceType.getName(VmDeviceType.DISK), VmDeviceType.getName(VmDeviceType.DISK));
        Set<Guid> pluggedDiskIds = new HashSet<Guid>();
        for (VmDevice diskVmDevice : disksVmDevices) {
            if (diskVmDevice.getIsPlugged()) {
                pluggedDiskIds.add(diskVmDevice.getDeviceId());
            }
        }
        return pluggedDiskIds;
    }
}

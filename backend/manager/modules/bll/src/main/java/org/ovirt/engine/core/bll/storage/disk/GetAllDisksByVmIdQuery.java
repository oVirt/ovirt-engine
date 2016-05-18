package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

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
        List<Disk> disks = new ArrayList<>();
        for (Disk disk : allDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE
                    || disk.getDiskStorageType() == DiskStorageType.CINDER) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(getAllImageSnapshots(diskImage));
            }
            disk.setDiskVmElements(Collections.singletonList(getDiskVmElement(disk)));
            disks.add(disk);
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }

    private DiskVmElement getDiskVmElement(BaseDisk disk) {
        return getDbFacade().getDiskVmElementDao().get(new VmDeviceId(disk.getId(), getParameters().getId()));
    }
}

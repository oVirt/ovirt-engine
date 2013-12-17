package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
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
        List<Disk> disks = new ArrayList<Disk>();
        for (Disk disk : allDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(getAllImageSnapshots(diskImage));
            }
            disks.add(disk);
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }
}

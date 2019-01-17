package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

public class GetAllDisksByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    DiskImageDao diskImageDao;

    public GetAllDisksByVmIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> allDisks = diskDao.getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered());
        List<Disk> disks = new ArrayList<>();
        for (Disk disk : allDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE
                    || disk.getDiskStorageType() == DiskStorageType.CINDER
                    || disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.getSnapshots().addAll(diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId()));
            }
            DiskVmElement dve = getDiskVmElement(disk);
            if (dve != null) {
                disk.setDiskVmElements(Collections.singletonList(dve));
                disks.add(disk);
            }
        }
        getQueryReturnValue().setReturnValue(disks);
    }

    private DiskVmElement getDiskVmElement(BaseDisk disk) {
        return diskVmElementDao.get(new VmDeviceId(disk.getId(), getParameters().getId()));
    }
}

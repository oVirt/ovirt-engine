package org.ovirt.engine.core.bll.storage.disk;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;

public class GetDiskAndSnapshotsByDiskIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    @Inject
    private ImagesHandler imagesHandler;

    public GetDiskAndSnapshotsByDiskIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> allDisks = diskDao.getAllFromDisksIncludingSnapshotsByDiskId(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered());

        // In case of LUN disk
        if (allDisks.size() == 1 && allDisks.get(0).getDiskStorageType() == DiskStorageType.LUN) {
            getQueryReturnValue().setReturnValue(allDisks.get(0));
            return;
        }

        // In case of disk without snapshots still need to aggregate the disk with its base image
        DiskImage diskWithSnapshots = imagesHandler.aggregateDiskImagesSnapshots(allDisks.stream()
                .map(DiskImage.class::cast)
                .collect(Collectors.toList())).stream().findFirst().orElse(null);

        getQueryReturnValue().setReturnValue(diskWithSnapshots);
    }
}

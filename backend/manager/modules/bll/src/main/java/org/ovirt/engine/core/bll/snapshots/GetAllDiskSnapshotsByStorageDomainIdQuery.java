package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllDiskSnapshotsByStorageDomainIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllDiskSnapshotsByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<DiskImage> diskImages =
                getDbFacade().getDiskImageDao().getAllSnapshotsForStorageDomain(getParameters().getId());

        // Filter out active volumes
        diskImages = diskImages.stream().filter(d -> !d.getActive()).collect(Collectors.toList());

        // Retrieving snapshots objects for setting description
        Map<Guid, Snapshot> snapshots = Entities.businessEntitiesById(
                getDbFacade().getSnapshotDao().getAllByStorageDomain(getParameters().getId()));

        List<DiskImage> diskImagesToReturn = new ArrayList<>();
        for (final DiskImage diskImage : diskImages) {
            Snapshot snapshot = snapshots.get(diskImage.getVmSnapshotId());
            // Verify snapshot is not null to mitigate possible race conditions
            if (snapshot != null) {
                diskImage.setVmSnapshotDescription(snapshot.getDescription());
                diskImagesToReturn.add(diskImage);
            }
        }

        getQueryReturnValue().setReturnValue(diskImagesToReturn);
    }
}

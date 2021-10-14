package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.DiskSnapshotsQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetAllDiskSnapshotsByStorageDomainIdQuery<P extends DiskSnapshotsQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private SnapshotDao snapshotDao;

    public GetAllDiskSnapshotsByStorageDomainIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<DiskImage> diskImages = diskImageDao.getAllSnapshotsForStorageDomain(getParameters().getId());

        if (!getParameters().getIncludeActive()) {
            // Filter out active volumes for backward compatibility.
            diskImages = diskImages.stream().filter(d -> !d.getActive()).collect(Collectors.toList());
        }

        // Retrieving snapshots objects for setting description
        Map<Guid, Snapshot> snapshots =
                Entities.businessEntitiesById(snapshotDao.getAllByStorageDomain(getParameters().getId()));

        List<DiskImage> diskImagesToReturn = new ArrayList<>();
        for (final DiskImage diskImage : diskImages) {
            Snapshot snapshot = snapshots.get(diskImage.getVmSnapshotId());
            // Verify snapshot is not null to mitigate possible race conditions
            if (snapshot != null) {
                diskImage.setVmSnapshotDescription(snapshot.getDescription());
                diskImage.setSnapshotCreationDate(snapshot.getCreationDate());
                diskImagesToReturn.add(diskImage);
            } else if (getParameters().getIncludeTemplate() && diskImage.isTemplate()) {
                // Template images may be required for reconstructing the snapshot chain, in case a VM was created
                // from a template with Thin Storage Allocation. Return them if 'include_template' param is sent
                diskImage.setVmSnapshotDescription("Template image");
                diskImagesToReturn.add(diskImage);
            }
        }

        getQueryReturnValue().setReturnValue(diskImagesToReturn);
    }
}

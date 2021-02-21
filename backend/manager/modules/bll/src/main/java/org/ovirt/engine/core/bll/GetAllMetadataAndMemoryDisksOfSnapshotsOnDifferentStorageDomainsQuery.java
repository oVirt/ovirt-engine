package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;

public class GetAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomainsQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private SnapshotDao snapshotDao;

    public GetAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomainsQuery(
            P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid storageDomainId = getParameters().getId();
        List<Guid> metadataAndMemoryVolumesOnSd =
                diskImageDao.getAllMetadataAndMemoryDisksForStorageDomain(storageDomainId);
        List<Guid> volumesOfSnapshotsOnSd = getAllVolumesOfSnapshotsForStorageDomain(storageDomainId);

        // Remove all metadata and memory volumes located on the same storage domain as their snapshots.
        metadataAndMemoryVolumesOnSd.removeAll(volumesOfSnapshotsOnSd);
        setReturnValue(metadataAndMemoryVolumesOnSd);
    }

    private List<Guid> getAllVolumesOfSnapshotsForStorageDomain(Guid storageDomainId) {
        List<Snapshot> snapshotsOnSd = snapshotDao.getAllByStorageDomain(storageDomainId);
        List<Guid> volumesOfSnapshotsOnSd = new ArrayList<>();

        snapshotsOnSd.stream()
                .filter(Snapshot::containsMemory)
                .forEach(snapshot -> {
                    volumesOfSnapshotsOnSd.add(snapshot.getMemoryDiskId());
                    volumesOfSnapshotsOnSd.add(snapshot.getMetadataDiskId());
                });
        return volumesOfSnapshotsOnSd;
    }
}

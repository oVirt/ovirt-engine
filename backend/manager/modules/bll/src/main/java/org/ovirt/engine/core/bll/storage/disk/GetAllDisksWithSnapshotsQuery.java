package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.DiskDao;

public class GetAllDisksWithSnapshotsQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private DiskDao diskDao;

    @Inject
    private ImagesHandler imagesHandler;

    public GetAllDisksWithSnapshotsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Collection<Disk> allDisksAndSnapshots =
                diskDao.getAllFromDisksIncludingSnapshots(getUserID(), getParameters().isFiltered());

        Collection<Disk> result = aggregateDisksSnapshots(allDisksAndSnapshots);
        getQueryReturnValue().setReturnValue(result);
    }

    /**
     * Gets a List of Disk objects, which include also the snapshots of the disks.
     * Aggregate for each disk its snapshots.
     *
     * @param allDisksAndSnapshots
     *            List of Disk objects to aggregate their snapshots
     * @return List of Disk objects
     */
    protected Collection<Disk> aggregateDisksSnapshots(Collection<Disk> allDisksAndSnapshots) {
        Map<Boolean, List<Disk>> mapSplittedByDiskAllowSnapsots =
                allDisksAndSnapshots.stream().collect(Collectors.partitioningBy(Disk::isAllowSnapshot));

        Collection<Disk> result = new ArrayList<>(mapSplittedByDiskAllowSnapsots.get(false));

        result.addAll(imagesHandler.aggregateDiskImagesSnapshots(mapSplittedByDiskAllowSnapsots.get(true).stream()
                .map(DiskImage.class::cast).collect(Collectors.toList())));

        return result;
    }
}

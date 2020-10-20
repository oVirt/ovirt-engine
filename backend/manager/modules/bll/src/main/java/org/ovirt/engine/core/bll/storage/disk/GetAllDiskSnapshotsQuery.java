package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;

public class GetAllDiskSnapshotsQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DiskDao diskDao;

    public GetAllDiskSnapshotsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Collection<Disk> diskAndSnapshots =
                diskDao.getAllFromDisksIncludingSnapshotsByDiskId(getParameters().getId(), getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(new ArrayList<>(DisksFilter.filterImageDisks(diskAndSnapshots, DisksFilter.ONLY_DISK_SNAPSHOT)));
    }
}

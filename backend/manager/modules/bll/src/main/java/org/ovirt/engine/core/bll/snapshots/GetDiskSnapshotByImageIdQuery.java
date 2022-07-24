package org.ovirt.engine.core.bll.snapshots;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;

public class GetDiskSnapshotByImageIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageDao imageDao;

    public GetDiskSnapshotByImageIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        DiskImage diskImage = diskImageDao.getSnapshotById(getParameters().getId());
        if (diskImage == null) {
            getQueryReturnValue().setReturnValue(null);
            return;
        }
        diskImage.setParentDiskId(imageDao.get(diskImage.getParentId()).getDiskId());

        getQueryReturnValue().setReturnValue(diskImage);
    }
}

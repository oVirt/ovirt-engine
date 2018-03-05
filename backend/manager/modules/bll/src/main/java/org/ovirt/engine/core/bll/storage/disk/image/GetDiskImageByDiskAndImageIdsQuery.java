package org.ovirt.engine.core.bll.storage.disk.image;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDiskImageByDiskAndImageIdsParameters;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetDiskImageByDiskAndImageIdsQuery<P extends GetDiskImageByDiskAndImageIdsParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DiskImageDao diskImageDao;

    public GetDiskImageByDiskAndImageIdsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskImageDao.getDiskImageByDiskAndImageIds(
                getParameters().getDiskId(), getParameters().getImageId(), getUserID(), getParameters().isFiltered()));
    }
}

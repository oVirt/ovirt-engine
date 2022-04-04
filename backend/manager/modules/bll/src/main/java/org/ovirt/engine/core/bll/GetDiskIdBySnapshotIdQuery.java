package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;

public class GetDiskIdBySnapshotIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private ImageDao imageDao;

    public GetDiskIdBySnapshotIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Image image = imageDao.get(getParameters().getId());
        if (image != null) {
            getQueryReturnValue().setReturnValue(image.getDiskId());
        } else {
            getQueryReturnValue().setReturnValue(Guid.Empty);
        }
    }

}

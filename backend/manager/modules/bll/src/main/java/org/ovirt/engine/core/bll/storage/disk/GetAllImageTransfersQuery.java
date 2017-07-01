package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.ImageTransferDao;

public class GetAllImageTransfersQuery<P extends QueryParametersBase>
        extends QueriesCommandBase<P> {

    public GetAllImageTransfersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    ImageTransferDao imageTransferDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(imageTransferDao.getAll());
    }
}

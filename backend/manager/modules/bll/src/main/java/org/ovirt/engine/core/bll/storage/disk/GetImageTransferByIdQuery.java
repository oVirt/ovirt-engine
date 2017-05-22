package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ImageTransferDao;

public class GetImageTransferByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ImageTransferDao imageTransferDao;

    public GetImageTransferByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(imageTransferDao.get(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}

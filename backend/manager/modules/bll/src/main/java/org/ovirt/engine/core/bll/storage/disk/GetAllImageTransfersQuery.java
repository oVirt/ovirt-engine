package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.ImageTransferDao;

public class GetAllImageTransfersQuery<P extends VdcQueryParametersBase>
        extends QueriesCommandBase<P> {

    public GetAllImageTransfersQuery(P parameters) {
        super(parameters);
    }

    @Inject
    ImageTransferDao imageTransferDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(imageTransferDao.getAll());
    }
}

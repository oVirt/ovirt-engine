package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskSnapshotByImageIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetDiskSnapshotByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskImageDao().getSnapshotById(getParameters().getId()));
    }
}

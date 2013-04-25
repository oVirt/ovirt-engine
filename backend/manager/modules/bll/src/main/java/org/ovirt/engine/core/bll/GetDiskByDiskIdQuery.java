package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskByDiskIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetDiskByDiskIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered() ));
    }
}

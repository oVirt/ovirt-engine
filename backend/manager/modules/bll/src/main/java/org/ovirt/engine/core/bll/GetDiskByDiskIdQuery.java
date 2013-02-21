package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetDiskByDiskIdParameters;

public class GetDiskByDiskIdQuery <P extends GetDiskByDiskIdParameters> extends QueriesCommandBase<P> {

    public GetDiskByDiskIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskDao().get(getParameters().getDiskId(), getUserID(), getParameters().isFiltered() ));
    }
}

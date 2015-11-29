package org.ovirt.engine.core.bll.storage.disk;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetDiskByDiskIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetDiskByDiskIdQuery(P parameters) {
        super(parameters);
    }

    public GetDiskByDiskIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered() ));
    }
}

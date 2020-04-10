package org.ovirt.engine.core.bll.storage.disk;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.TransferDiskImageCommand;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetImageioProxyUriQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    public GetImageioProxyUriQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TransferDiskImageCommand.getProxyUri());
    }
}

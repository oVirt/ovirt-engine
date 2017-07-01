package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetUserBySessionIdQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    public GetUserBySessionIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getUser());
    }

}

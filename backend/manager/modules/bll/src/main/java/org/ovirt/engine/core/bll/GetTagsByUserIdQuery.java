package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTagsByUserIdQuery<P extends GetTagsByUserIdParameters> extends
        QueriesCommandBase<P> {
    public GetTagsByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getTagDao()
                        .getAllForUsers(getParameters().getUserId()));
    }
}

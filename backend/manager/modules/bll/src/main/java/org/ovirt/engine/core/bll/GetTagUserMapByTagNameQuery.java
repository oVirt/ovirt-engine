package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

// NOT IN USE
public class GetTagUserMapByTagNameQuery<P extends GetTagUserMapByTagNameParameters> extends QueriesCommandBase<P> {
    public GetTagUserMapByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getTagDAO().getTagUserMapByTagName(getParameters().getTagName()));
    }
}

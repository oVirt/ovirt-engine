package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetTagsByVdsIdQuery<P extends GetTagsByVdsIdParameters> extends
        QueriesCommandBase<P> {
    public GetTagsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getTagDao()
                        .getAllForVds(getParameters().getVdsId()));
    }
}

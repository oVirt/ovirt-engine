package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTagsByVmIdQuery<P extends GetTagsByVmIdParameters> extends
        QueriesCommandBase<P> {
    public GetTagsByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getTagDao()
                        .getAllForVm(getParameters().getVmId()));
    }
}

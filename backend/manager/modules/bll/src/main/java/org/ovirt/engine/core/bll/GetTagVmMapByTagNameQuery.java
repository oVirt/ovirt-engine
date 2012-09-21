package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagVmMapByTagNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

// NOT IN USE
public class GetTagVmMapByTagNameQuery<P extends GetTagVmMapByTagNameParameters> extends QueriesCommandBase<P> {
    public GetTagVmMapByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getTagDao().getTagVmMapByTagName(getParameters().getTagName()));
    }
}

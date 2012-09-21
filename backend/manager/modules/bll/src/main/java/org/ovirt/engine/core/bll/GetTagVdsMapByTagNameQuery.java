package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagVdsMapByTagNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

// NOT IN USE
public class GetTagVdsMapByTagNameQuery<P extends GetTagVdsMapByTagNameParameters> extends QueriesCommandBase<P> {
    public GetTagVdsMapByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().
                setReturnValue(DbFacade.getInstance().getTagDao().getTagVdsMapByTagName(getParameters().getTagName()));
    }
}

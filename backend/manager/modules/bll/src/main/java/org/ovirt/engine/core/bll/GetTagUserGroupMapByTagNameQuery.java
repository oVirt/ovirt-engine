package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagUserGroupMapByTagNameParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

// NOT IN USE
public class GetTagUserGroupMapByTagNameQuery<P extends GetTagUserGroupMapByTagNameParameters>
        extends QueriesCommandBase<P> {
    public GetTagUserGroupMapByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getTagDAO().getTagUserGroupMapsForTagName(getParameters().getTagName()));
    }
}

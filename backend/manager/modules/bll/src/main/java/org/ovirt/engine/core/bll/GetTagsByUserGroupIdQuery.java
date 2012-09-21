package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetTagsByUserGroupIdQuery<P extends GetTagsByUserGroupIdParameters>
        extends QueriesCommandBase<P> {
    public GetTagsByUserGroupIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance()
                                .getTagDao()
                                .getAllForUserGroups(getParameters().getGroupId()));
    }
}

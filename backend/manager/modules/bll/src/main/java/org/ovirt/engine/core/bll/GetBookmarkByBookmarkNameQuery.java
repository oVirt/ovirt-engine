package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetBookmarkByBookmarkNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetBookmarkByBookmarkNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getBookmarkDao().getByName(getParameters().getName()));
    }

}

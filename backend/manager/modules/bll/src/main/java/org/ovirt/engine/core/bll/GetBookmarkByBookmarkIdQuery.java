package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetBookmarkByBookmarkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetBookmarkByBookmarkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getBookmarkDao().get(getParameters().getId()));
    }

}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetAllBookmarksQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllBookmarksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getBookmarkDAO().getAll());
    }
}

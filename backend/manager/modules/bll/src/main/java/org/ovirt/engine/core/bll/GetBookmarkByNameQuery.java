package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetBookmarkByNameQuery<P extends GetBookmarkByNameParameters> extends QueriesCommandBase<P> {
    public GetBookmarkByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        GetBookmarkByNameParameters params = (GetBookmarkByNameParameters) getParameters();
        Bookmark bookmark = DbFacade.getInstance().getBookmarkDao()
                .getByName(params.getBookmarkName());

        getQueryReturnValue().setReturnValue(bookmark);
    }
}

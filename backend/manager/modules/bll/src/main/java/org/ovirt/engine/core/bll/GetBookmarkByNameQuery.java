package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetBookmarkByNameQuery<P extends GetBookmarkByNameParameters> extends QueriesCommandBase<P> {
    public GetBookmarkByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        GetBookmarkByNameParameters params = (GetBookmarkByNameParameters) getParameters();
        bookmarks bookmark = DbFacade.getInstance().getBookmarkDAO()
                .getByName(params.getBookmarkName());

        getQueryReturnValue().setReturnValue(bookmark);
    }
}

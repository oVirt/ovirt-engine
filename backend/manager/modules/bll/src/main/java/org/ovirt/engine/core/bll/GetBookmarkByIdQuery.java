package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

// NOT IN USE
public class GetBookmarkByIdQuery<P extends GetBookmarkByIdParameters> extends QueriesCommandBase<P> {
    public GetBookmarkByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        bookmarks bookmark = DbFacade.getInstance().getBookmarkDao().get(getParameters().getBookmarkId());

        getQueryReturnValue().setReturnValue(bookmark);
    }
}

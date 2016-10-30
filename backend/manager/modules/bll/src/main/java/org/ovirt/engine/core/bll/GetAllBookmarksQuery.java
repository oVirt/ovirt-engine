package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.BookmarkDao;

public class GetAllBookmarksQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllBookmarksQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private BookmarkDao bookmarkDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(bookmarkDao.getAll());
    }
}

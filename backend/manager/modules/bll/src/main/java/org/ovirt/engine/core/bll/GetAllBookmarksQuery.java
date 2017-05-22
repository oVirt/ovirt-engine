package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.BookmarkDao;

public class GetAllBookmarksQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllBookmarksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private BookmarkDao bookmarkDao;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(bookmarkDao.getAll());
    }
}

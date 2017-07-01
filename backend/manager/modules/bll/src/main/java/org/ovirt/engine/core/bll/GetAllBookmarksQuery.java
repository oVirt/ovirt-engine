package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.BookmarkDao;

public class GetAllBookmarksQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
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

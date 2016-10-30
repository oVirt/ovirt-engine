package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.BookmarkDao;

public class GetBookmarkByBookmarkNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private BookmarkDao bookmarkDao;

    public GetBookmarkByBookmarkNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(bookmarkDao.getByName(getParameters().getName()));
    }

}

package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetFilteredAndSortedParameters;
import org.ovirt.engine.core.dao.VmPoolDao;

public class GetAllVmPoolsFilteredAndSortedQuery <P extends GetFilteredAndSortedParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmPoolDao vmPoolDao;

    public GetAllVmPoolsFilteredAndSortedQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        //get max results and page # from parameters
        int maxResults = getParameters().getMaxResults();
        int pageNum = getParameters().getPageNum();

        //translate them to offset and limit (for the database)
        int offset = (pageNum - 1) * maxResults;
        int limit = offset + maxResults;
        setReturnValue(vmPoolDao.getAllVmPoolsFilteredAndSorted(getUserID(), offset, limit));
    }

}

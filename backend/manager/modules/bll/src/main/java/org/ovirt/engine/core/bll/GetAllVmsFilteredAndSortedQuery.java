package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetFilteredAndSortedParameters;

public class GetAllVmsFilteredAndSortedQuery<P extends GetFilteredAndSortedParameters> extends GetAllVmsQueryBase<P> {

    public GetAllVmsFilteredAndSortedQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected List<VM> getVMs() {
        //get max results and page # from parameters
        int maxResults = getParameters().getMaxResults();
        int pageNum = getParameters().getPageNum();

        //translate them to offset and limit (for the database)
        int offset = (pageNum - 1) * maxResults;
        int limit = offset + maxResults;
        return vmDao.getAllSortedAndFiltered(getUserID(), offset, limit);
    }

}

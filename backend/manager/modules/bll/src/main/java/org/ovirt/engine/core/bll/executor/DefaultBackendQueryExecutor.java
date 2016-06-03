package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

@Alternative
public class DefaultBackendQueryExecutor implements BackendQueryExecutor {

    @Override
    public VdcQueryReturnValue execute(final QueriesCommandBase<?> query, final VdcQueryType queryType) {
        query.execute();
        return query.getQueryReturnValue();
    }
}

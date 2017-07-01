package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

@Alternative
public class DefaultBackendQueryExecutor implements BackendQueryExecutor {

    @Override
    public QueryReturnValue execute(final QueriesCommandBase<?> query, final QueryType queryType) {
        query.execute();
        return query.getQueryReturnValue();
    }
}

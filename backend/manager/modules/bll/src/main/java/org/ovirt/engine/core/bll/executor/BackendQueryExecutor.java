package org.ovirt.engine.core.bll.executor;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public interface BackendQueryExecutor {

    QueryReturnValue execute(QueriesCommandBase<?> command, QueryType queryType);
}

package org.ovirt.engine.core.bll.executor;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public interface BackendQueryExecutor {

    VdcQueryReturnValue execute(QueriesCommandBase<?> command, QueryType queryType);
}

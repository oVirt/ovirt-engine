package org.ovirt.engine.core.bll.executor;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public interface BackendQueryExecutor {

    VdcQueryReturnValue execute(QueriesCommandBase<?> command, VdcQueryType queryType);
}

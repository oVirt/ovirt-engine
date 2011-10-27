package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.IRegisterQueryUpdatedData;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public interface IBackendCallBackServer {
    void QueryDataChanged(Guid queryId, IRegisterQueryUpdatedData updatedData);

    void QueryFailed(Guid queryId);

    Guid BackendException(VdcActionType actionType, VdcFault fault);

    /**
     * Add fault search query to queue of async queries
     *
     * @param queryId
     *            - guid of fault query
     * @param queryType
     *            - the type of query
     * @param fault
     *            - the type of fault
     */
    void SearchQueryException(Guid queryId, VdcQueryType queryType, VdcFault fault);

    void ClearQuery(Guid QueryId);
}

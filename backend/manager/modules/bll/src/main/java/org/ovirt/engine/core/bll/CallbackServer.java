package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.interfaces.IBackendCallBackServer;
import org.ovirt.engine.core.common.queries.AsyncQueryResults;
import org.ovirt.engine.core.common.queries.IRegisterQueryUpdatedData;
import org.ovirt.engine.core.common.queries.ListIVdcQueryableUpdatedData;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NotImplementedException;

public class CallbackServer implements IBackendCallBackServer {
    public static final CallbackServer Instance = new CallbackServer();

    public void QueryDataChanged(Guid queryId, IRegisterQueryUpdatedData updatedData) {
        synchronized (queries) {
            if (!queries.containsKey(queryId)) {
                queries.put(queryId, new java.util.ArrayList<ListIVdcQueryableUpdatedData>());
            }
            queries.get(queryId).add((ListIVdcQueryableUpdatedData) updatedData);

        }
    }

    public void QueryFailed(Guid queryId) {
        throw new NotImplementedException();
    }

    public Guid BackendException(org.ovirt.engine.core.common.action.VdcActionType actionType,
                                 org.ovirt.engine.core.common.errors.VdcFault fault) {
        Guid FaultQueryId = Guid.NewGuid();
        log.debugFormat("CallbackServer:VdcFaultMessage: action={0} Fault={1}", actionType.name(), fault.getMessage());
        synchronized (queries) {
            queries.put(FaultQueryId, new java.util.ArrayList<ListIVdcQueryableUpdatedData>());
            ListIVdcQueryableUpdatedData faultData = new ListIVdcQueryableUpdatedData();
            faultData.setFaulted(new ValueObjectPair(actionType, fault));
            queries.get(FaultQueryId).add(faultData);

        }
        return FaultQueryId;

    }

    @Override
    public void SearchQueryException(Guid queryId, VdcQueryType queryType, VdcFault fault) {
        synchronized (queries) {
            queries.put(queryId, new java.util.ArrayList<ListIVdcQueryableUpdatedData>());
            ListIVdcQueryableUpdatedData faultData = new ListIVdcQueryableUpdatedData();
            faultData.setFaulted(new ValueObjectPair(queryType, fault));
            queries.get(queryId).add(faultData);
        }
    }

    public AsyncQueryResults GetAsyncQueryResults(Guid[] queryIDs) {
        java.util.ArrayList<KeyValuePairCompat<Guid, ArrayList<ListIVdcQueryableUpdatedData>>> allResults =
                new ArrayList<KeyValuePairCompat<Guid, ArrayList<ListIVdcQueryableUpdatedData>>>();
        synchronized (queries) {
            for (Guid queryId : queryIDs) {
                ArrayList<ListIVdcQueryableUpdatedData> result = new ArrayList<ListIVdcQueryableUpdatedData>();
                if (queries.containsKey(queryId)) {
                    result = queries.get(queryId);
                    queries.put(queryId, new java.util.ArrayList<ListIVdcQueryableUpdatedData>());
                }
                allResults.add(new KeyValuePairCompat<Guid, ArrayList<ListIVdcQueryableUpdatedData>>(queryId, result));

            }
        }

        return new AsyncQueryResults(allResults.toArray(new KeyValuePairCompat[0]));
    }

    private java.util.HashMap<Guid, java.util.ArrayList<ListIVdcQueryableUpdatedData>> queries =
            new java.util.HashMap<Guid, java.util.ArrayList<ListIVdcQueryableUpdatedData>>();

    private static LogCompat log = LogFactoryCompat.getLog(CallbackServer.class);

    // need to remove old queries from queue of registeres searches
    public void ClearQuery(Guid QueryId) {
        synchronized (queries) {
            queries.remove(QueryId);
        }
    }
}

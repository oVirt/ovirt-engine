package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.ovirt.engine.core.common.interfaces.IBackendCallBackServer;
import org.ovirt.engine.core.common.queries.IRegisterQueryUpdatedData;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;

/**
 * Class, responcible to communication with frontends. Contain all search
 * requests, recieved from single frontend
 */
public class CallBackData {

    private static LogCompat log = LogFactoryCompat.getLog(CallBackData.class);
    private static final String QUERY_TYPE_UNKNOWN = "Unknown";

    private String privateSessionId;
    private IBackendCallBackServer _callback;
    private final java.util.HashMap<Guid, QueryData> _queries = new java.util.HashMap<Guid, QueryData>();
    private AtomicBoolean duringRefresh = new AtomicBoolean();
    public AtomicInteger RefreshCyclesWithoutPolling = new AtomicInteger();

    public CallBackData(IBackendCallBackServer callBack, String sessionId) {
        _callback = callBack;
        privateSessionId = sessionId;
    }

    public void RegisterQuery(Guid QueryId, QueryData queryData) {
        synchronized (_queries) {
            _queries.put(QueryId, queryData);
        }
    }

    public void UnregisterQuery(Guid queryID) {
        synchronized (_queries) {
            if (_queries.containsKey(queryID)) {
                if (log.isDebugEnabled()) {
                    String queryType = QUERY_TYPE_UNKNOWN;
                    if (_queries.get(queryID) != null)
                        queryType = _queries.get(queryID).getQueryType().toString();
                    log.debugFormat("Frontend {0} will unregister from query. Query Id: {1}, query type: {2}",
                            getSessionId(), queryID, queryType);
                }
                _queries.remove(queryID);
                this.getCallback().ClearQuery(queryID);
            } else {
                log.errorFormat("Frontend {0} tried to unregister from query which was not registered. Query Id: {1}",
                        getSessionId(), queryID);
            }
        }
    }

    public void RefreshQueries() {
        synchronized (_queries) {
            for (QueryData queryData : _queries.values()) {
                // Faults (if registered) will have null QueryData, so skip them
                if (queryData == null)
                    continue;
                refreshQuery(queryData);
            }
        }
    }

    private void refreshQuery(QueryData queryData) {
        java.util.ArrayList<QueryData> toRemove = new java.util.ArrayList<QueryData>();
        RefObject<IRegisterQueryUpdatedData> updatedData = new RefObject<IRegisterQueryUpdatedData>();
        RefObject<Boolean> changed = new RefObject<Boolean>();

        if (!queryData.RefreshQuery(updatedData, changed)) {
            toRemove.add(queryData);
        }
        queryData.setInitialized(true);

        if (changed.argvalue) {
            getCallback().QueryDataChanged(queryData.getQueryId(), updatedData.argvalue);
            if (log.isDebugEnabled()) {
                log.debugFormat("Session {0}: Query {1} Results Updated.", getSessionId(), queryData.getQueryId());
            }
        }
        for (QueryData dataToRemove : toRemove) {
            QueryData data = _queries.remove(dataToRemove.getQueryId());
            if (data != null) {
                this.getCallback().ClearQuery(dataToRemove.getQueryId());
                log.errorFormat(
                        "Frontend unregistered forcibly from query due illegal query. Query Id: {0}, Query Type: {1}",
                        dataToRemove.getQueryId(), dataToRemove.getQueryType());
            }
        }
    }

    public String getSessionId() {
        return privateSessionId;
    }

    public int getQueriesCount() {
        return _queries.size();
    }

    public IBackendCallBackServer getCallback() {
        return _callback;
    }

    public Guid[] getQueryIDs() {
        synchronized (_queries) {
            List<Guid> list = new ArrayList<Guid>();
            for (Guid key : _queries.keySet()) {
                QueryData data = _queries.get(key);
                if (data == null || data.isInitialized()) {
                    list.add(key);
                }
            }
            Guid[] keys = new Guid[list.size()];
            list.toArray(keys);
            return keys;
        }
    }

    public void resetDuringRefresh() {
        duringRefresh.set(false);
    }

    public boolean compareAndSetDuringRefresh() {
        return duringRefresh.compareAndSet(false, true);
    }
}

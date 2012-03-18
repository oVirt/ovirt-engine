package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.interfaces.IBackendCallBackServer;
import org.ovirt.engine.core.common.queries.AsyncQueryResults;
import org.ovirt.engine.core.common.queries.RegisterQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

/**
 * Responsible for backend callbacks treatment. Contains proxies to frontends, registered to backend events
 */
public final class BackendCallBacksDirector {
    private static final BackendCallBacksDirector _instance = new BackendCallBacksDirector();
    private static Log log = LogFactory.getLog(BackendCallBacksDirector.class);
    private static final HashMap<String, CallBackData> _callBacks = new HashMap<String, CallBackData>();

    public static BackendCallBacksDirector getInstance() {
        return _instance;
    }

    private BackendCallBacksDirector() {
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "QueriesRefreshTimer_Elapsed", new Class[0],
                new Object[0], Config.<Integer> GetValue(ConfigValues.SearchesRefreshRateInSeconds),
                Config.<Integer> GetValue(ConfigValues.SearchesRefreshRateInSeconds), TimeUnit.SECONDS);
    }

    @OnTimerMethodAnnotation("QueriesRefreshTimer_Elapsed")
    public void QueriesRefreshTimer_Elapsed() {
        CountDownLatch latch;
        List<String> callBacksToRemove;
        synchronized (_callBacks) {
            if (_callBacks.size() > 0) {
                latch = new CountDownLatch(_callBacks.size());
                callBacksToRemove = Collections.synchronizedList(new ArrayList<String>());
            } else {
                return;
            }
            refreshQueries(latch, callBacksToRemove);
        }

        try {
            // TODO move this to config
            latch.await(3, TimeUnit.MINUTES);
            if (callBacksToRemove.size() > 0) {
                synchronized (_callBacks) {
                    for (String callBackToRemove : callBacksToRemove) {
                        _callBacks.remove(callBackToRemove);
                    }
                }
            }
        } catch (InterruptedException e) {

        }
    }

    private boolean refreshCallBackQuery(CallBackData callBackData) {
        if (callBackData != null) {
            boolean toRemove = false;

            try {
                int refreshCyclesSinceLastPolling = callBackData.RefreshCyclesWithoutPolling.incrementAndGet();
                if (refreshCyclesSinceLastPolling < Config
                        .<Integer> GetValue(ConfigValues.AsyncPollingCyclesBeforeRefreshSuspend)) {
                    callBackData.RefreshQueries();
                }
                // }
                else if (refreshCyclesSinceLastPolling == Config
                        .<Integer> GetValue(ConfigValues.AsyncPollingCyclesBeforeRefreshSuspend))
                    log.debugFormat(
                            "Client did not poll async queries updates for {1} cycles, suspending server side updates for session id = {0}",
                            callBackData.getSessionId(),
                            Config.<Integer> GetValue(ConfigValues.AsyncPollingCyclesBeforeRefreshSuspend));
                else if (refreshCyclesSinceLastPolling > Config
                        .<Integer> GetValue(ConfigValues.AsyncPollingCyclesBeforeCallbackCleanup)) // enough,
                                                                                                   // assume
                                                                                                   // client
                                                                                                   // is
                                                                                                   // gone
                {
                    log.debugFormat(
                            "Client did not poll async queries updates for {1} cycles sessionId = {0}. Callback will be removed.",
                            callBackData.getSessionId(),
                            Config.<Integer> GetValue(ConfigValues.AsyncPollingCyclesBeforeCallbackCleanup));
                    toRemove = true;
                }
            } catch (RuntimeException ex) {
                log.infoFormat("Communication with client has aborted sessionId = {0}. Callback will be removed.",
                        callBackData.getSessionId());
                if (Config.<Boolean> GetValue(ConfigValues.DebugSearchLogging)) {
                    log.info("Problem with refreshing callback query", ex);
                }
                toRemove = true;
            }
            if (toRemove) {
                    for (Guid QueryId : callBackData.getQueryIDs()) {
                        callBackData.UnregisterQuery(QueryId);
                    }
                }
            return toRemove;
        }
        return false;
    }

    private void refreshQueries(final CountDownLatch latch, final List<String> callBacksToRemove) {
        for (final CallBackData callBackData : _callBacks.values()) {
            if (callBackData.compareAndSetDuringRefresh()) {
                ThreadPoolUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean toRemove;
                            synchronized (callBackData) {
                                toRemove = refreshCallBackQuery(callBackData);
                            }
                            if (toRemove) {
                                callBacksToRemove.add(callBackData.getSessionId());
                            }
                        } finally {
                            callBackData.resetDuringRefresh();
                            latch.countDown();
                        }
                    }
                });
            }
        }
    }

    public void RegisterQuery(RegisterQueryParameters parameters) {
        CallBackData callBack = null;
        synchronized (_callBacks) {
            if (_callBacks.containsKey(ThreadLocalParamsContainer.getHttpSessionId())) {
                callBack = _callBacks.get(ThreadLocalParamsContainer.getHttpSessionId());
            } else {
                IBackendCallBackServer backendCallBack = CallbackServer.Instance;
                if (backendCallBack != null) {
                    callBack = new CallBackData(backendCallBack, ThreadLocalParamsContainer.getHttpSessionId());
                    _callBacks.put(callBack.getSessionId(), callBack);

                    if (Config.<Boolean> GetValue(ConfigValues.DebugSearchLogging)) {
                        log.infoFormat("Frontend {0} has registered for queries", callBack.getSessionId());
                    }
                }
            }
        }

        if (callBack != null) {
            synchronized (callBack) {
                final QueryData queryData =
                        QueryData.CreateQueryData(parameters.getQueryID(), parameters.getQueryType(),
                                parameters.getQueryParams());
                callBack.RegisterQuery(queryData.getQueryId(), queryData);
                if (Config.<Boolean> GetValue(ConfigValues.DebugSearchLogging)) {
                    log.infoFormat("Frontend {0} has registered to query. Query Id: {1}, Query type: {2}.",
                            callBack.getSessionId(), queryData.getQueryId(), queryData.getQueryType());
                }
            }
        }
    }

    public void UnregisterQuery(Guid queryID) {
        CallBackData callBack = null;
        synchronized (_callBacks) {
            callBack = _callBacks.get(ThreadLocalParamsContainer
                    .getHttpSessionId());
        }
        if (callBack != null) {
            synchronized (callBack) {
                callBack.UnregisterQuery(queryID);
                if (Config.<Boolean> GetValue(ConfigValues.DebugSearchLogging)) {
                    log.infoFormat("Frontend {0} has unregistered from query. Query Id: {1}.", callBack.getSessionId(),
                            queryID);
                }
                if (callBack.getQueriesCount() == 0) {
                    if (Config.<Boolean> GetValue(ConfigValues.DebugSearchLogging)) {
                        log.infoFormat("Frontend {0} unregistered from all queries - removing callback.",
                                callBack.getSessionId());
                    }
                    synchronized (_callBacks) {
                        _callBacks.remove(ThreadLocalParamsContainer.getHttpSessionId());
                    }
                }
            }
        }
    }

    public AsyncQueryResults GetAsyncQueryResults() {
        try {
            CallBackData callBack;
            synchronized (_callBacks) {
                callBack = _callBacks.get(ThreadLocalParamsContainer.getHttpSessionId());
            }
            // if no session, add a backend exception on session to be returned
            // to client
            NGuid FaultQueryId = null;
            if (callBack == null) {
                log.warnFormat("BackendCallbacksDirector.AsyncQueryResults - unknown callbackData for session");
                callBack = new CallBackData(CallbackServer.Instance, null);
                VdcFault fault = new VdcFault();
                fault.setError(VdcBllErrors.SESSION_ERROR);
                fault.setMessage("Unkown session, please login again.");
                FaultQueryId = CallbackServer.Instance.BackendException(
                        VdcActionType.LoginAdminUser, fault);
                callBack.RegisterQuery(FaultQueryId.getValue(), null);
            } else {
                synchronized (callBack) {
                    callBack.RefreshCyclesWithoutPolling.set(0);
                    Guid[] guidArray = callBack.getQueryIDs();
                    if (guidArray.length > 0) {
                        AsyncQueryResults results = CallbackServer.Instance.GetAsyncQueryResults(guidArray);
                        for (int i = 0; i < results.getQueryData().size(); i++) {
                            if (results.getQueryData().get(i).getValue().size() == 1
                                    && results.getQueryData().get(i).getValue().get(0).getFaulted() != null) {
                                UnregisterQuery(results.getQueryIDs().get(i));
                            }
                        }
                        return results;
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Unexpected error in BackendCallBacksDirector.GetAsyncQueryResults", ex);
            throw new RuntimeException(ex);
        }
        return new AsyncQueryResults();
    }

    public void RegisterFaultQuery(Guid FaultQueryId, String sessionId) {
        try {
            CallBackData callBack;
            synchronized (_callBacks) {
                callBack = _callBacks.get(sessionId);
            }
            if (callBack != null) {
                synchronized (callBack) {
                    callBack.RegisterQuery(FaultQueryId, null);
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in BackendCallBacksDirector.RegisterFaultQuery", ex);
            throw new RuntimeException(ex);
        }
    }

    public void UnregisterFaultQuery(Guid FaultQueryId) {
        try {
            CallBackData callBack;
            synchronized (_callBacks) {
                callBack = _callBacks.get(ThreadLocalParamsContainer.getHttpSessionId());
            }
            if (callBack != null) {
                synchronized (callBack) {
                    callBack.UnregisterQuery(FaultQueryId);
                }
            }
        } catch (Exception ex) {
            log.error("Unexpected error in BackendCallBacksDirector.UnregisterFaultQuery", ex);
            throw new RuntimeException(ex);
        }
    }

}

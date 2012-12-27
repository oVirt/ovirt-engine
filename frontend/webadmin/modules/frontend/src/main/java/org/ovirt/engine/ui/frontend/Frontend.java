package org.ovirt.engine.ui.frontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

public class Frontend {
    static class QueryWrapper {
        private VdcQueryType queryType;
        private VdcQueryParametersBase parameters;
        private AsyncQuery callback;

        private String queryKey;

        public QueryWrapper(VdcQueryType queryType,
                VdcQueryParametersBase parameters,
                AsyncQuery callback) {
            this.queryType = queryType;
            this.parameters = parameters;
            this.callback = callback;
            String sender = callback.getModel() == null ? "" : callback.getModel().toString();

            if (queryType == VdcQueryType.Search && parameters instanceof SearchParameters) {
                queryKey = queryType.toString() + ((SearchParameters) parameters).getSearchPattern()
                        + parameters.getClass().toString() +
                        sender;
            } else {
                queryKey = queryType.toString() + parameters.getClass().toString() +
                        sender;
            }
        }

        public VdcQueryType getQueryType() {
            return queryType;
        }

        public VdcQueryParametersBase getParameters() {
            return parameters;
        }

        public AsyncQuery getCallback() {
            return callback;
        }

        public String getKey() {
            return queryKey;
        }
    }

    private static final String RPC_TIMEOUT_EXCEPTION_STATUS_CODE_PREFIX = "120"; //$NON-NLS-1$

    private static Logger logger = Logger.getLogger(Frontend.class.getName());
    private static IFrontendEventsHandler eventsHandler;
    private static FrontendLoginHandler loginHandler;
    private static VdcUser loggedUser;
    private static ErrorTranslator canDoActionErrorsTranslator =
            new ErrorTranslator((AppErrors) GWT.create(AppErrors.class));
    private static ErrorTranslator vdsmErrorsTranslator =
            new ErrorTranslator((VdsmErrors) GWT.create(VdsmErrors.class));

    private static final Map<String, QueryWrapper> currentRequests = new HashMap<String, Frontend.QueryWrapper>();
    private static final Map<String, QueryWrapper> pendingRequests = new HashMap<String, Frontend.QueryWrapper>();

    private static VdcQueryType[] subscribedQueryTypes;

    public static EventDefinition QueryStartedEventDefinition = new EventDefinition("QueryStarted", Frontend.class); //$NON-NLS-1$
    public static Event QueryStartedEvent = new Event(QueryStartedEventDefinition);

    public static EventDefinition QueryCompleteEventDefinition = new EventDefinition("QueryComplete", Frontend.class); //$NON-NLS-1$
    public static Event QueryCompleteEvent = new Event(QueryCompleteEventDefinition);

    private static Event frontendFailureEvent = new Event("FrontendFailure", Frontend.class); //$NON-NLS-1$

    private static Event frontendNotLoggedInEvent = new Event("NotLoggedIn", Frontend.class); //$NON-NLS-1$

    public static Event getFrontendFailureEvent() {
        return frontendFailureEvent;
    }

    public static Event getFrontendNotLoggedInEvent() {
        return frontendNotLoggedInEvent;
    }

    public static IFrontendEventsHandler getEventsHandler() {
        return eventsHandler;
    }

    public static void setEventsHandler(IFrontendEventsHandler eventsHandler) {
        Frontend.eventsHandler = eventsHandler;
    }

    public static FrontendLoginHandler getLoginHandler() {
        return loginHandler;
    }

    public static void setLoginHandler(FrontendLoginHandler loginHandler) {
        Frontend.loginHandler = loginHandler;
    }

    private static void translateErrors(List<VdcReturnValueBase> errors) {
        for (VdcReturnValueBase retVal : errors) {
            retVal.setCanDoActionMessages(canDoActionErrorsTranslator.translateErrorText(retVal.getCanDoActionMessages()));
        }
    }

    private static void failureEventHandler(String errorMessage) {
        failureEventHandler(null, errorMessage);
    }

    private static void failureEventHandler(Throwable caught) {
        String errorMessage;
        if (caught instanceof StatusCodeException) {
            errorMessage =
                    ConstantsManager.getInstance().getConstants().requestToServerFailedWithCode()+": " //$NON-NLS-1$
                            + ((StatusCodeException) caught).getStatusCode();
        } else {
            errorMessage = ConstantsManager.getInstance().getConstants().requestToServerFailed() +": " + caught.getLocalizedMessage(); //$NON-NLS-1$
        }
        failureEventHandler(errorMessage);
    }

    private static void failureEventHandler(String description, String errorMessage) {
        handleNotLoggedInEvent(errorMessage);
        frontendFailureEvent.raise(Frontend.class, new FrontendFailureEventArgs(new Message(description, errorMessage)));
    }

    private static void failureEventHandler(String description, ArrayList<String> errorMessages) {
        ArrayList<Message> messages = new ArrayList<Message>();
        for (String errorMessage : errorMessages) {
            messages.add(new Message(description, errorMessage));
        }

        frontendFailureEvent.raise(Frontend.class, new FrontendFailureEventArgs(messages));
    }

    private static void handleNotLoggedInEvent(String errorMessage) {
        if (errorMessage != null && errorMessage.equals("USER_IS_NOT_LOGGED_IN")) { //$NON-NLS-1$
            frontendNotLoggedInEvent.raise(Frontend.class, EventArgs.Empty);
        }
    }

    private static boolean filterQueries;

    public static void setFilterQueries(boolean filterQueries) {
        Frontend.filterQueries = filterQueries;
    }

    private static void initQueryParamsFilter(VdcQueryParametersBase parameters) {
        parameters.setFiltered(filterQueries);
    }

    public static void RunQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final AsyncQuery callback) {
        final QueryWrapper queryWrapper = new QueryWrapper(queryType, parameters, callback);
        final boolean isHandleSequentialQueries = isHandleSequentialQueries(queryWrapper);
        if (isHandleSequentialQueries) {
            if (currentRequests.get(queryWrapper.getKey()) == null) {
                currentRequests.put(queryWrapper.getKey(), queryWrapper);
            } else {
                pendingRequests.put(queryWrapper.getKey(), queryWrapper);
                return;
            }
        }

        initQueryParamsFilter(parameters);
        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runQuery."); //$NON-NLS-1$
        raiseQueryStartedEvent(queryType, callback.getContext());
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                try {
                    if (ignoreFailure(caught)) {
                        return;
                    }
                    logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                    getEventsHandler().runQueryFailed(null);
                    failureEventHandler(caught);
                    if (callback.isHandleFailure()) {
                        callback.asyncCallback.OnSuccess(callback.getModel(), null);
                    }
                    raiseQueryCompleteEvent(queryType, callback.getContext());
                } finally {
                    if (isHandleSequentialQueries) {
                        handleSequentialQueries(queryWrapper.getKey());
                    }
                }
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                try {
                    logger.finer("Succesful returned result from RunQuery."); //$NON-NLS-1$

                    if (!result.getSucceeded()) {
                        logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString() //$NON-NLS-1$
                                + "]"); //$NON-NLS-1$
                        if (getEventsHandler() != null) {
                            ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                            failedResult.add(result);
                            // getEventsHandler().runQueryFailed(failedResult);
                            String errorMessage = result.getExceptionString();
                            handleNotLoggedInEvent(errorMessage);
                        }
                        if (callback.isHandleFailure()) {
                            callback.getDel().OnSuccess(callback.getModel(), result);
                        }
                    } else {
                        callback.setOriginalReturnValue(result);
                        if (callback.getConverter() != null) {
                            callback.getDel().OnSuccess(callback.getModel(),
                                    callback.getConverter().Convert(result.getReturnValue(), callback));
                        }
                        else {
                            callback.getDel().OnSuccess(callback.getModel(), result);
                        }
                    }

                    raiseQueryCompleteEvent(queryType, callback.getContext());
                }
                finally {
                    if (isHandleSequentialQueries) {
                        handleSequentialQueries(queryWrapper.getKey());
                    }
                }
            }

            private void handleSequentialQueries(String key) {
                currentRequests.remove(queryWrapper.getKey());

                QueryWrapper wrapper = pendingRequests.get(key);
                if (wrapper != null) {
                    pendingRequests.remove(queryWrapper.getKey());
                    RunQuery(wrapper.getQueryType(), wrapper.getParameters(), wrapper.getCallback());
                }
            }

        });
    }

    private static boolean isHandleSequentialQueries(QueryWrapper queryWrapper) {
        // currently we support only search, because we can id the search pattern and sender
        return queryWrapper.getQueryType() == VdcQueryType.Search &&
                queryWrapper.getParameters() instanceof SearchParameters;
    }

    public static VdcQueryReturnValue RunQuery(VdcQueryType queryType, VdcQueryParametersBase parameters) {
        logger.fine("Sync RunQuery is invoked, this is not supported! replace the call with the async method!"); //$NON-NLS-1$
        return null;
    }

    public static void RunPublicQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final AsyncQuery callback) {
        initQueryParamsFilter(parameters);
        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runQuery."); //$NON-NLS-1$

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunPublicQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    callback.asyncCallback.OnSuccess(callback.getModel(), null);
                }
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunQuery."); //$NON-NLS-1$

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString() //$NON-NLS-1$
                            + "]"); //$NON-NLS-1$
                    if (getEventsHandler() != null) {

                        ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                        failedResult.add(result);
                        getEventsHandler().runQueryFailed(failedResult);
                        // if (getEventsHandler().isRaiseErrorModalPanel(queryType))
                        failureEventHandler(result.getExceptionString());
                        if (callback.isHandleFailure()) {
                            callback.getDel().OnSuccess(callback.getModel(), result);
                        }
                    }
                    if (callback.isHandleFailure()) {
                        callback.getDel().OnSuccess(callback.getModel(), result);
                    }
                } else {
                    if (callback.getConverter() != null) {
                        callback.getDel().OnSuccess(callback.getModel(),
                                callback.getConverter().Convert(result.getReturnValue(), callback));
                    }
                    else {
                        callback.getDel().OnSuccess(callback.getModel(), result);
                    }
                }
            }
        });
    }

    public static VdcQueryReturnValue RunPublicQuery(VdcQueryType queryType, VdcQueryParametersBase parameters) {
        logger.warning("Frontend: RunPublicQuery -sync- was executed, this method is not supported!"); //$NON-NLS-1$
        return null;
    }

    public static void RunMultipleQueries(final ArrayList<VdcQueryType> queryTypeList,
            final ArrayList<VdcQueryParametersBase> queryParamsList,
            final IFrontendMultipleQueryAsyncCallback callback) {
        RunMultipleQueries(queryTypeList, queryParamsList, callback, null);
    }

    public static void RunMultipleQueries(final ArrayList<VdcQueryType> queryTypeList,
            final ArrayList<VdcQueryParametersBase> queryParamsList,
            final IFrontendMultipleQueryAsyncCallback callback,
            final String context) {
        logger.finer("Frontend: Invoking async runMultipleQueries."); //$NON-NLS-1$

        raiseQueryStartedEvent(queryTypeList, context);
        for (VdcQueryParametersBase parameters : queryParamsList) {
            parameters.setRefresh(false);
            initQueryParamsFilter(parameters);
        }

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunMultipleQueries(queryTypeList, queryParamsList, new AsyncCallback<ArrayList<VdcQueryReturnValue>>() {
            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunPublicQuery: " + caught, caught); //$NON-NLS-1$
                FrontendMultipleQueryAsyncResult f =
                        new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, null);
                failureEventHandler(caught);
                callback.Executed(f);

                raiseQueryCompleteEvent(queryTypeList, context);
            }

            @Override
            public void onSuccess(ArrayList<VdcQueryReturnValue> result) {
                logger.finer("Succesful returned result from RunMultipleQueries!"); //$NON-NLS-1$
                FrontendMultipleQueryAsyncResult f =
                        new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, result);

                for (VdcQueryReturnValue currQRV : result) {
                    if (!currQRV.getSucceeded()) {
                        logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + currQRV.getExceptionString() //$NON-NLS-1$
                                + "]"); //$NON-NLS-1$

                        if (getEventsHandler() != null) {
                            // TODO: This should be handled better
                            // getEventsHandler().runQueryFailed(result);
                            // if (getEventsHandler()
                            // .isRaiseErrorModalPanel(null))
                            // failureEventHandler("Failed to execute RunMultipleQueries!");
                        }

                        callback.Executed(f);
                        return;
                    }
                }

                callback.Executed(f);

                raiseQueryCompleteEvent(queryTypeList, context);
            }
        });
    }

    private static String currentContext;

    public static String getCurrentContext() {
        return currentContext;
    }

    private static void raiseQueryEvent(final Event queryEvent, final VdcQueryType queryType, final String context) {
        if (context != null && subscribedQueryTypes != null)
        {
            for (VdcQueryType vdcQueryType : subscribedQueryTypes)
            {
                if (queryType.equals(vdcQueryType))
                {
                    currentContext = context;
                    queryEvent.raise(Frontend.class, EventArgs.Empty);
                }
            }
        }
    }

    private static void raiseQueryStartedEvent(final VdcQueryType queryType, final String context) {
        raiseQueryEvent(getQueryStartedEvent(), queryType, context);
    }

    private static void raiseQueryCompleteEvent(final VdcQueryType queryType, final String context) {
        raiseQueryEvent(getQueryCompleteEvent(), queryType, context);
    }

    private static void raiseQueryStartedEvent(final ArrayList<VdcQueryType> queryTypeList, final String context) {
        for (VdcQueryType queryType : queryTypeList) {
            raiseQueryStartedEvent(queryType, context);
        }
    }

    private static void raiseQueryCompleteEvent(final ArrayList<VdcQueryType> queryTypeList, final String context) {
        for (VdcQueryType queryType : queryTypeList) {
            raiseQueryCompleteEvent(queryType, context);
        }
    }

    public static void RunAction(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback) {
        RunAction(actionType, parameters, callback, null);
    }

    public static void RunAction(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback,
            final Object state) {
        logger.finer("Invoking async runAction."); //$NON-NLS-1$
        dumpActionDetails(actionType, parameters);

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunAction(actionType, parameters, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught); //$NON-NLS-1$
                failureEventHandler(caught);
                FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, null, state);
                callback.Executed(f);
            }

            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!"); //$NON-NLS-1$
                handleActionResult(actionType, parameters, result, callback, state);
            }
        });
    }

    public static VdcReturnValueBase RunAction(final VdcActionType actionType, final VdcActionParametersBase parameters) {
        logger.warning("Sync RunAction is invoked, this is not supported! replace the call with the async method!"); //$NON-NLS-1$
        dumpActionDetails(actionType, parameters);

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.RunAction(actionType, parameters, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught); //$NON-NLS-1$
                failureEventHandler(caught);
                failureEventHandler(caught);
            }

            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!"); //$NON-NLS-1$
                handleActionResult(actionType, parameters, result, null, null);
            }
        });

        return new VdcReturnValueBase();
    }

    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state) {
        RunMultipleAction(actionType, parameters, false, callback, state);
    }

    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters,
            boolean isRunOnlyIfAllCanDoPass,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.RunMultipleActions(actionType, parameters, isRunOnlyIfAllCanDoPass, new AsyncCallback<ArrayList<VdcReturnValueBase>>() {
            @Override
            public void onFailure(Throwable caught) {
                        if (ignoreFailure(caught)) {
                            return;
                        }
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught); //$NON-NLS-1$
                failureEventHandler(caught);

                if (callback != null) {
                    callback.Executed(new FrontendMultipleActionAsyncResult(actionType, parameters, null, state));
                }
            }

            @Override
            public void onSuccess(ArrayList<VdcReturnValueBase> result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!"); //$NON-NLS-1$

                ArrayList<VdcReturnValueBase> failed = new ArrayList<VdcReturnValueBase>();
                ArrayList<VdcFault> faults = new ArrayList<VdcFault>();

                for (VdcReturnValueBase v : result) {
                    if (!v.getCanDoAction()) {
                        failed.add(v);
                    }
                }

                if (!failed.isEmpty()) {
                    translateErrors(failed);
                    getEventsHandler().runMultipleActionFailed(actionType, failed, faults);
                }

                if (callback != null) {
                    callback.Executed(new FrontendMultipleActionAsyncResult(actionType, parameters, result, state));
                }
            }
        });
    }

    public static ArrayList<VdcReturnValueBase> RunMultipleAction(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters) {
        RunMultipleAction(actionType, parameters, null, null);
        return null;
    }

    public static void RunMultipleActions(final ArrayList<VdcActionType> actionTypes,
            final ArrayList<VdcActionParametersBase> parameters,
            final ArrayList<IFrontendActionAsyncCallback> callbacks,
            final IFrontendActionAsyncCallback failureCallback,
            final Object state)
    {
        if (actionTypes.isEmpty() || parameters.isEmpty() || callbacks.isEmpty())
        {
            return;
        }

        RunAction(actionTypes.get(0), parameters.get(0),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        boolean success = returnValue != null && returnValue.getSucceeded();
                        if (success || failureCallback == null)
                        {
                            IFrontendActionAsyncCallback callback = callbacks.get(0);
                            if (callback != null)
                            {
                                callback.Executed(result);
                            }
                            actionTypes.remove(0);
                            parameters.remove(0);
                            callbacks.remove(0);
                            RunMultipleActions(actionTypes, parameters, callbacks, failureCallback, state);
                        }
                        else
                        {
                            failureCallback.Executed(result);
                        }
                    }
                }, state);
    }

    public static void UnregisterQuery(Guid asyncSearchId) {
        throw new NotImplementedException("UnregisterQuery is not implemented!"); //$NON-NLS-1$
    }

    public static void LoginAsync(final String userName, final String password, final String domain, final AsyncQuery callback) {
        logger.finer("Frontend: Invoking async Login."); //$NON-NLS-1$

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.Login(userName, password, domain, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Login."); //$NON-NLS-1$
                setLoggedInUser(null);
                List<VdcReturnValueBase> failed = new ArrayList<VdcReturnValueBase>();
                failed.add(result);
                translateErrors(failed);
                callback.asyncCallback.OnSuccess(callback.getModel(), result);
                if (getLoginHandler() != null && result.getSucceeded()) {
                    getLoginHandler().onLoginSuccess(userName, password, domain);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    setLoggedInUser(null);
                    callback.asyncCallback.OnSuccess(callback.getModel(), null);
                }
            }
        });
    }

    public static void LogoffAsync(VdcUser vdcUser, final AsyncQuery callback) {
        logger.finer("Frontend: Invoking async Logoff."); //$NON-NLS-1$

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.logOff(vdcUser, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Logoff."); //$NON-NLS-1$
                callback.asyncCallback.OnSuccess(callback.getModel(), result);
                if (getLoginHandler() != null) {
                    getLoginHandler().onLogout();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute Logoff: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                callback.asyncCallback.OnSuccess(callback.getModel(), null);
            }
        });
    }

    public static Boolean getIsUserLoggedIn() {
        return getLoggedInUser() != null;
    }

    public static void getLoggedInUser(final IAsyncCallback<VdcUser> callback) {
        logger.finer("Determining whether user is logged in..."); //$NON-NLS-1$

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.getLoggedInUser(new AsyncCallback<VdcUser>() {
            @Override
            public void onFailure(Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute sync getIsUserLoggedIn: " + caught, caught); //$NON-NLS-1$
                failureEventHandler(caught);
                callback.OnFailure(null);
            }

            @Override
            public void onSuccess(VdcUser result) {
                logger.finer("Sucessfully executed sync getIsUserLoggedIn!"); //$NON-NLS-1$
                callback.OnSuccess(result);
            }
        });
    }

    public static VdcUser getLoggedInUser() {
        return loggedUser;
    }

    public static void setLoggedInUser(VdcUser loggedUser) {
        Frontend.loggedUser = loggedUser;
    }

    // TODO: Externalize to a better location, should support translation via
    // resource bundle file.
    private static String getRunActionErrorMessage(java.util.ArrayList<String> messages) {
        if (messages.size() < 1) {
            return "No Message"; //$NON-NLS-1$
        } else {
            return messages.iterator().next();
        }
    }

    private static void dumpQueryDetails(VdcQueryType queryType, VdcQueryParametersBase searchParameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("VdcQuery Type: '" + queryType + "', ");  //$NON-NLS-1$//$NON-NLS-2$
        if (searchParameters instanceof SearchParameters) {
            SearchParameters sp = (SearchParameters) searchParameters;

            if (sp.getSearchPattern().equals("Not implemented")) { //$NON-NLS-1$
                throw new RuntimeException("Search pattern is defined as 'Not implemented', probably because of a use of String.format()"); //$NON-NLS-1$
            }

            sb.append("Type value: [" + sp.getSearchTypeValue() + "], Pattern: [" + sp.getSearchPattern() + "]");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        } else {
            sb.append("Search type is base or unknown"); //$NON-NLS-1$
        }

        logger.fine(sb.toString());
    }

    private static void dumpActionDetails(VdcActionType actionType, VdcActionParametersBase parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("actionType Type: '" + actionType + "', ");  //$NON-NLS-1$//$NON-NLS-2$
        sb.append("Params: " + parameters); //$NON-NLS-1$

        logger.fine(sb.toString());
    }

    private static void handleActionResult(VdcActionType actionType,
            VdcActionParametersBase parameters,
            VdcReturnValueBase result,
            IFrontendActionAsyncCallback callback,
            final Object state) {
        logger.log(Level.FINER, "Retrieved action result from RunAction."); //$NON-NLS-1$

        FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, result, state);
        boolean success = false;
        if (!result.getCanDoAction()) {
            List<VdcReturnValueBase> failed = new ArrayList<VdcReturnValueBase>();
            failed.add(result);
            translateErrors(failed);
            getEventsHandler().runActionFailed(failed);
            if (callback != null)
                callback.Executed(f);
        } else if (result.getIsSyncronious() && result.getSucceeded() == false) {
            runActionExecutionFailed(actionType, result.getFault());
            if (callback != null)
                callback.Executed(f);

            // Prevent another (untranslated) error message pop-up display
            // ('runActionExecutionFailed' invokes an error pop-up displaying,
            // therefore calling 'failureEventHandler' is redundant)
            success = true;
        } else {
            success = true;
            if (callback != null)
                callback.Executed(f);
        }

        if ((!success) && (getEventsHandler() != null) && (getEventsHandler().isRaiseErrorModalPanel(actionType, result.getFault()))) {
            if (result.getCanDoActionMessages().size() <= 1) {
                String errorMessage = !result.getCanDoAction() || !result.getCanDoActionMessages().isEmpty() ?
                        getRunActionErrorMessage(result.getCanDoActionMessages()) : result.getFault().getMessage();

                failureEventHandler(result.getDescription(), errorMessage);
            }
            else {
                failureEventHandler(result.getDescription(), result.getCanDoActionMessages());
            }
        }
    }

    private static void runActionExecutionFailed(VdcActionType actionType, VdcFault fault) {
        if (getEventsHandler() != null) {
            fault.setMessage(vdsmErrorsTranslator.TranslateErrorTextSingle(fault.getMessage()));
            getEventsHandler().runActionExecutionFailed(actionType, fault);
        }
    }

    public static RegistrationResult RegisterQuery(VdcQueryType queryType,
            VdcQueryParametersBase paramenters) {
        // TODO Auto-generated method stub
        return null;
    }

    public static RegistrationResult RegisterSearch(String searchString,
            SearchType entityType, int searchPageSize) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void Subscribe(VdcQueryType[] queryTypes)
    {
        subscribedQueryTypes = queryTypes;
    }

    public static void Unsubscribe()
    {
        subscribedQueryTypes = null;
        currentContext = null;

        QueryStartedEvent.getListeners().clear();
        QueryCompleteEvent.getListeners().clear();
    }

    public static Event getQueryStartedEvent() {
        return QueryStartedEvent;
    }

    public static Event getQueryCompleteEvent() {
        return QueryCompleteEvent;
    }

    // ignore escape key
    protected static boolean ignoreFailure(Throwable caught) {
        if (caught instanceof StatusCodeException && ((StatusCodeException)
                caught).getStatusCode() == 0) {
            return true;
        }
        return false;
    }

}

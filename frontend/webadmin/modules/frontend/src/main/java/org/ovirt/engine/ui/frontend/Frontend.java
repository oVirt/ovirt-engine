package org.ovirt.engine.ui.frontend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

public class Frontend {
    static class QueryWrapper {
        private VdcQueryType queryType;
        private VdcQueryParametersBase parameters;
        private AsyncQuery callback;

        private String queryKey;

        public QueryWrapper(VdcQueryType queryType, VdcQueryParametersBase parameters, AsyncQuery callback) {
            this.queryType = queryType;
            this.parameters = parameters;
            this.callback = callback;
            String sender = callback.getModel() == null ? "" : callback.getModel().toString();

            if (queryType == VdcQueryType.Search && parameters instanceof SearchParameters) {
                queryKey =
                        queryType.toString() + ((SearchParameters) parameters).getSearchPattern()
                                + parameters.getClass().toString() + sender;
            } else {
                queryKey = queryType.toString() + parameters.getClass().toString() + sender;
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

    private static Logger logger = Logger.getLogger(Frontend.class.getName());
    private static IFrontendEventsHandler eventsHandler;
    private static FrontendLoginHandler loginHandler;
    private static VdcUser loggedUser;
    private static ErrorTranslator canDoActionErrorsTranslator = null;
    private static ErrorTranslator vdsmErrorsTranslator = null;

    static UIConstants constants = null;
    static final Map<String, QueryWrapper> currentRequests = new HashMap<String, Frontend.QueryWrapper>();
    static final Map<String, QueryWrapper> pendingRequests = new HashMap<String, Frontend.QueryWrapper>();

    private static VdcQueryType[] subscribedQueryTypes;

    public final static EventDefinition QueryStartedEventDefinition = new EventDefinition("QueryStarted", Frontend.class); //$NON-NLS-1$
    public static Event QueryStartedEvent = new Event(QueryStartedEventDefinition);

    public final static EventDefinition QueryCompleteEventDefinition = new EventDefinition("QueryComplete", Frontend.class); //$NON-NLS-1$
    public static Event QueryCompleteEvent = new Event(QueryCompleteEventDefinition);

    static Event frontendFailureEvent = new Event("FrontendFailure", Frontend.class); //$NON-NLS-1$

    static Event frontendNotLoggedInEvent = new Event("NotLoggedIn", Frontend.class); //$NON-NLS-1$

    /**
     * empty callback.
     */
    private final static NullableFrontendActionAsyncCallback NULLABLE_ASYNC_CALLBACK = new NullableFrontendActionAsyncCallback();

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

    public static ErrorTranslator getAppErrorsTranslator() {
        if (canDoActionErrorsTranslator == null) {
            setAppErrorsTranslator(new ErrorTranslator((AppErrors) GWT.create(AppErrors.class)));
        }
        return canDoActionErrorsTranslator;
    }

    protected static void setAppErrorsTranslator(ErrorTranslator translator) {
        canDoActionErrorsTranslator = translator;
    }

    private static void translateErrors(List<VdcReturnValueBase> errors) {
        for (VdcReturnValueBase retVal : errors) {
            retVal.setCanDoActionMessages((ArrayList<String>) translateError(retVal));
        }
    }

    private static List<String> translateError(VdcReturnValueBase error) {
        return getAppErrorsTranslator().translateErrorText(error.getCanDoActionMessages());
    }

    private static void failureEventHandler(String errorMessage) {
        failureEventHandler(null, errorMessage);
    }

    private static void failureEventHandler(Throwable caught) {
        String errorMessage;
        if (caught instanceof StatusCodeException) {
            errorMessage = getConstants().requestToServerFailedWithCode() + ": " //$NON-NLS-1$
                    + ((StatusCodeException) caught).getStatusCode();
        } else {
            errorMessage =
                    getConstants().requestToServerFailed()
                            + ": " + caught.getLocalizedMessage(); //$NON-NLS-1$
        }
        failureEventHandler(errorMessage);
    }

    static UIConstants getConstants() {
        if (constants == null) {
            constants = ConstantsManager.getInstance().getConstants();
        }
        return constants;
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
        if ("USER_IS_NOT_LOGGED_IN".equals(errorMessage)) { //$NON-NLS-1$
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
        final GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        RunQuery(queryType, parameters, service, callback);
    }

    protected static void RunQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final GenericApiGWTServiceAsync service,
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
        service.RunQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(final Throwable caught) {
                try {
                    if (ignoreFailure(caught)) {
                        return;
                    }
                    logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                    getEventsHandler().runQueryFailed(null);
                    failureEventHandler(caught);
                    if (callback.isHandleFailure()) {
                        callback.getDel().onSuccess(callback.getModel(), null);
                    }
                } finally {
                    raiseQueryCompleteEvent(queryType, callback.getContext());
                    if (isHandleSequentialQueries) {
                        handleSequentialQueries(queryWrapper.getKey());
                    }
                }
            }

            @Override
            public void onSuccess(final VdcQueryReturnValue result) {
                try {
                    logger.finer("Succesful returned result from RunQuery."); //$NON-NLS-1$

                    if (!result.getSucceeded()) {
                        logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" //$NON-NLS-1$
                                + result.getExceptionString() + "]"); //$NON-NLS-1$
                        if (getEventsHandler() != null) {
                            ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                            failedResult.add(result);
                            // getEventsHandler().runQueryFailed(failedResult);
                            String errorMessage = result.getExceptionString();
                            handleNotLoggedInEvent(errorMessage);
                        }
                        if (callback.isHandleFailure()) {
                            callback.getDel().onSuccess(callback.getModel(), result);
                        }
                    } else {
                        callback.setOriginalReturnValue(result);
                        if (callback.getConverter() != null) {
                            callback.getDel().onSuccess(callback.getModel(),
                                    callback.getConverter().Convert(result.getReturnValue(), callback));
                        } else {
                            callback.getDel().onSuccess(callback.getModel(), result);
                        }
                    }

                    raiseQueryCompleteEvent(queryType, callback.getContext());
                } finally {
                    if (isHandleSequentialQueries) {
                        handleSequentialQueries(queryWrapper.getKey());
                    }
                }
            }

            private void handleSequentialQueries(final String key) {
                currentRequests.remove(queryWrapper.getKey());

                QueryWrapper wrapper = pendingRequests.get(key);
                if (wrapper != null) {
                    pendingRequests.remove(queryWrapper.getKey());
                    RunQuery(wrapper.getQueryType(), wrapper.getParameters(), service, wrapper.getCallback());
                }
            }

        });
    }

    private static boolean isHandleSequentialQueries(final QueryWrapper queryWrapper) {
        // currently we support only search, because we can id the search pattern and sender
        return queryWrapper.getQueryType() == VdcQueryType.Search
                && queryWrapper.getParameters() instanceof SearchParameters;
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
            public void onFailure(final Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    callback.asyncCallback.onSuccess(callback.getModel(), null);
                }
            }

            @Override
            public void onSuccess(final VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunQuery."); //$NON-NLS-1$

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" //$NON-NLS-1$
                            + result.getExceptionString() + "]"); //$NON-NLS-1$
                    if (getEventsHandler() != null) {

                        ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                        failedResult.add(result);
                        getEventsHandler().runQueryFailed(failedResult);
                        // if (getEventsHandler().isRaiseErrorModalPanel(queryType))
                        failureEventHandler(result.getExceptionString());
                        if (callback.isHandleFailure()) {
                            callback.getDel().onSuccess(callback.getModel(), result);
                        }
                    }
                    if (callback.isHandleFailure()) {
                        callback.getDel().onSuccess(callback.getModel(), result);
                    }
                } else {
                    if (callback.getConverter() != null) {
                        callback.getDel().onSuccess(callback.getModel(),
                                callback.getConverter().Convert(result.getReturnValue(), callback));
                    } else {
                        callback.getDel().onSuccess(callback.getModel(), result);
                    }
                }
            }
        });
    }

    public static void RunMultipleQueries(final List<VdcQueryType> queryTypeList,
            final List<VdcQueryParametersBase> queryParamsList,
            final IFrontendMultipleQueryAsyncCallback callback) {
        RunMultipleQueries(queryTypeList, queryParamsList, callback, null);
    }

    public static void RunMultipleQueries(final List<VdcQueryType> queryTypeList,
            final List<VdcQueryParametersBase> queryParamsList,
            final IFrontendMultipleQueryAsyncCallback callback,
            final String context) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        RunMultipleQueries(queryTypeList, queryParamsList, callback, context, service);
    }

    public static void RunMultipleQueries(final List<VdcQueryType> queryTypeList,
            final List<VdcQueryParametersBase> queryParamsList,
            final IFrontendMultipleQueryAsyncCallback callback,
            final String context, final GenericApiGWTServiceAsync service) {
        logger.finer("Frontend: Invoking async runMultipleQueries."); //$NON-NLS-1$

        raiseQueryStartedEvent(queryTypeList, context);
        for (VdcQueryParametersBase parameters : queryParamsList) {
            parameters.setRefresh(false);
            initQueryParamsFilter(parameters);
        }

        service.RunMultipleQueries((ArrayList<VdcQueryType>) queryTypeList,
                (ArrayList<VdcQueryParametersBase>) queryParamsList,
                new AsyncCallback<ArrayList<VdcQueryReturnValue>>() {
            @Override
            public void onFailure(final Throwable caught) {
                try {
                    if (ignoreFailure(caught)) {
                        return;
                    }
                    logger.log(Level.SEVERE, "Failed to execute RunPublicQuery: " + caught, caught); //$NON-NLS-1$
                    FrontendMultipleQueryAsyncResult f =
                            new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, null);
                    failureEventHandler(caught);
                    callback.executed(f);
                } finally {
                    raiseQueryCompleteEvent(queryTypeList, context);
                }
            }

            @Override
            public void onSuccess(final ArrayList<VdcQueryReturnValue> result) {
                logger.finer("Succesful returned result from RunMultipleQueries!"); //$NON-NLS-1$
                FrontendMultipleQueryAsyncResult f =
                        new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, result);
                callback.executed(f);
                raiseQueryCompleteEvent(queryTypeList, context);
            }
        });
    }

    private static String currentContext;

    public static String getCurrentContext() {
        return currentContext;
    }

    private static void raiseQueryEvent(final Event queryEvent, final VdcQueryType queryType, final String context) {
        if (context != null && subscribedQueryTypes != null) {
            for (VdcQueryType vdcQueryType : subscribedQueryTypes) {
                if (queryType.equals(vdcQueryType)) {
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

    private static void raiseQueryStartedEvent(final List<VdcQueryType> queryTypeList, final String context) {
        for (VdcQueryType queryType : queryTypeList) {
            raiseQueryStartedEvent(queryType, context);
        }
    }

    private static void raiseQueryCompleteEvent(final List<VdcQueryType> queryTypeList, final String context) {
        for (VdcQueryType queryType : queryTypeList) {
            raiseQueryCompleteEvent(queryType, context);
        }
    }

    /**
     * Run an action of the specified action type using the passed in parameters. No object state.
     * @param actionType The action type of the action to perform.
     * @param parameters The parameters of the action.
     * @param callback The callback to call when the action is completed.
     */
    public static void RunAction(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback) {
        RunAction(actionType, parameters, callback, null);
    }

    /**
     * Run an action of the specified action type using the passed in parameters, also pass in a state object
     * @param actionType The action type of the action to perform.
     * @param parameters The parameters of the action.
     * @param callback The callback to call when the action is completed.
     * @param state The state object.
     */
    public static void RunAction(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback,
            final Object state) {
        RunAction(actionType, parameters, callback != null ? callback : NULLABLE_ASYNC_CALLBACK, state, true);
    }

    /**
     * Run an action of the specified action type using the passed in parameters, also pass in a state object
     * @param actionType The action type of the action to perform.
     * @param parameters The parameters of the action.
     * @param callback The callback to call when the action is completed.
     * @param state The state object.
     * @param showErrorDialog Whether to show a pop-up dialog with the error or not.
     */
    public static void RunAction(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback,
            final Object state,
            final boolean showErrorDialog) {
        final GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        runActionImpl(actionType, parameters, callback != null ? callback : NULLABLE_ASYNC_CALLBACK, state,
                showErrorDialog, service);
    }

    /**
     * Note: the given callback must not be null.
     * @param actionType The type of action to run.
     * @param parameters The parameters for the action.
     * @param callback The callback to call on failure.
     * @param state The state object.
     * @param showErrorDialog Whether to show a pop-up dialog with the error or not.
     */
    static void runActionImpl(final VdcActionType actionType,
            final VdcActionParametersBase parameters,
            final IFrontendActionAsyncCallback callback,
            final Object state,
            final boolean showErrorDialog,
            final GenericApiGWTServiceAsync service) {
        logger.finer("Invoking async runAction."); //$NON-NLS-1$
        dumpActionDetails(actionType, parameters);

        service.RunAction(actionType, parameters, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(final Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught); //$NON-NLS-1$
                failureEventHandler(caught);
                FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, null, state);
                callback.executed(f);
            }

            @Override
            public void onSuccess(final VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!"); //$NON-NLS-1$
                handleActionResult(actionType, parameters, result, callback, state, showErrorDialog);
            }
        });
    }

    /**
     * {@code RunAction} without callback.
     * @param actionType The action type of the action to run.
     * @param parameters The parameters to the action.
     * @param showErrorDialog Whether to show a pop-up dialog with the error or not.
     */
    public static void RunAction(final VdcActionType actionType, final VdcActionParametersBase parameters,
            final boolean showErrorDialog) {
        RunAction(actionType, parameters, Frontend.NULLABLE_ASYNC_CALLBACK, null, showErrorDialog);
    }

    /**
     * {@code RunAction} without callback.
     * @param actionType The action type of the action to run.
     * @param parameters The parameters to the action.
     */
    public static void RunAction(final VdcActionType actionType, final VdcActionParametersBase parameters) {
        RunAction(actionType, parameters, Frontend.NULLABLE_ASYNC_CALLBACK);
    }

    /**
     * Identical to 5 parameter RunMultipleAction, but isRunOnlyIfAllCanDoPass is false.
     * @param actionType The action type of the actions to run.
     * @param parameters The parameters to the actions.
     * @param callback The callback to call after the operation happens.
     * @param state A state object.
     */
    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state) {
        RunMultipleAction(actionType, parameters, false, callback, state);
    }

    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters,
            final boolean isRunOnlyIfAllCanDoPass,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        RunMultipleAction(actionType, parameters, isRunOnlyIfAllCanDoPass, callback, state, service);
    }

    static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters,
            final boolean isRunOnlyIfAllCanDoPass,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state, final GenericApiGWTServiceAsync service) {

        service.RunMultipleActions(actionType,
                parameters,
                isRunOnlyIfAllCanDoPass,
                new AsyncCallback<ArrayList<VdcReturnValueBase>>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        if (ignoreFailure(caught)) {
                            return;
                        }
                        logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught); //$NON-NLS-1$
                        failureEventHandler(caught);

                        if (callback != null) {
                            callback.executed(new FrontendMultipleActionAsyncResult(actionType, parameters, null,
                                    state));
                        }
                    }

                    @Override
                    public void onSuccess(final ArrayList<VdcReturnValueBase> result) {
                        logger.finer("Frontend: sucessfully executed RunAction, determining result!"); //$NON-NLS-1$

                        ArrayList<VdcReturnValueBase> failed = new ArrayList<VdcReturnValueBase>();

                        for (VdcReturnValueBase v : result) {
                            if (!v.getCanDoAction()) {
                                failed.add(v);
                            }
                        }

                        if (!failed.isEmpty()) {
                            translateErrors(failed);
                            getEventsHandler().runMultipleActionFailed(actionType, failed);
                        }

                        if (callback != null) {
                            callback.executed(new FrontendMultipleActionAsyncResult(actionType,
                                    parameters,
                                    result,
                                    state));
                        }
                    }
                });
    }

    /**
     * RunMultipleActions without passing in a callback or state.
     * @param actionType The type of action to perform.
     * @param parameters The parameters of the action.
     */
    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> parameters) {
        RunMultipleAction(actionType, parameters, null, null);
    }

    /**
     * Overloaded method for {@link #RunMultipleActions(VdcActionType, List, IFrontendActionAsyncCallback, Object)} with
     * state = null.
     */
    public static void RunMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            IFrontendActionAsyncCallback successCallback) {
        RunMultipleActions(actionType, parameters, successCallback, null);
    }

    /**
     * Overloaded method for {@link #RunMultipleActions(VdcActionType, List, List, Object)} with state = null.
     */
    public static void RunMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            List<IFrontendActionAsyncCallback> callbacks) {
        RunMultipleActions(actionType, parameters, callbacks, null);
    }

    /**
     * A convenience method that calls {@link #RunMultipleActions(VdcActionType, List, List, Object)} with just a single
     * callback to be called when all actions have succeeded.
     *
     * @param actionType
     *            the action to be repeated.
     * @param parameters
     *            the parameters of each action.
     * @param successCallback
     *            the callback to be executed when all actions have succeeded.
     * @param state
     *            the state.
     */
    public static void RunMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            IFrontendActionAsyncCallback successCallback,
            Object state) {

        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        int n = parameters.size();
        IFrontendActionAsyncCallback[] callbacks = new IFrontendActionAsyncCallback[n];
        callbacks[n-1] = successCallback;
        RunMultipleActions(actionType,
                parameters,
                new LinkedList<IFrontendActionAsyncCallback>(Arrays.asList(callbacks)),
                state);
    }

    /**
     * A convenience method that calls
     * {@link #RunMultipleActions(List, List, List, IFrontendActionAsyncCallback, Object)} with just the one
     * VdcActionType for all actions.
     *
     * @param actionType
     *            the action to be repeated.
     * @param parameters
     *            the parameters of each action.
     * @param callbacks
     *            the callback to be executed upon the success of each action.
     * @param state
     *            the state.
     */
    public static void RunMultipleActions(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            List<IFrontendActionAsyncCallback> callbacks,
            Object state) {

        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        VdcActionType[] actionTypes = new VdcActionType[parameters.size()];
        Arrays.fill(actionTypes, actionType);
        RunMultipleActions(new LinkedList<VdcActionType>(Arrays.asList(actionTypes)),
                parameters,
                callbacks,
                null,
                state);
    }

    /**
     * This method allows us to run a transaction like set of actions. If one
     * fails the rest do not get executed.
     * @param actionTypes The list of actions to execute.
     * @param parameters The list of parameters, must match the number of actions.
     * @param callbacks The list of callbacks, the number must match the number of actions.
     * @param failureCallback The callback to call in case of failure.
     * @param state The state.
     */
    public static void RunMultipleActions(final List<VdcActionType> actionTypes,
            final List<VdcActionParametersBase> parameters,
            final List<IFrontendActionAsyncCallback> callbacks,
            final IFrontendActionAsyncCallback failureCallback,
            final Object state) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        RunMultipleActions(actionTypes, parameters, callbacks, failureCallback, state, service);
    }

    /**
     * This method allows us to run a transaction like set of actions. If one
     * fails the rest do not get executed.
     * @param actionTypes The list of actions to execute.
     * @param parameters The list of parameters, must match the number of actions.
     * @param callbacks The list of callbacks, the number must match the number of actions.
     * @param failureCallback The callback to call in case of failure.
     * @param state The state.
     * @param service The service implementation to use.
     */
    static void RunMultipleActions(final List<VdcActionType> actionTypes,
            final List<VdcActionParametersBase> parameters,
            final List<IFrontendActionAsyncCallback> callbacks,
            final IFrontendActionAsyncCallback failureCallback,
            final Object state,
            final GenericApiGWTServiceAsync service) {
        if (actionTypes.isEmpty() || parameters.isEmpty() || callbacks.isEmpty())
        {
            return;
        }

        runActionImpl(actionTypes.get(0), parameters.get(0),
            new IFrontendActionAsyncCallback() {
                @Override
                public void executed(final FrontendActionAsyncResult result) {
                    VdcReturnValueBase returnValue = result.getReturnValue();
                    boolean success = returnValue != null && returnValue.getSucceeded();
                    if (success || failureCallback == null) {
                        IFrontendActionAsyncCallback callback = callbacks.get(0);
                        if (callback != null) {
                            callback.executed(result);
                        }
                        actionTypes.remove(0);
                        parameters.remove(0);
                        callbacks.remove(0);
                        RunMultipleActions(actionTypes, parameters, callbacks, failureCallback, state, service);
                    } else {
                        failureCallback.executed(result);
                    }
                }
            }, state, true, service);
    }

    /**
     * ASynchronous user login.
     * @param userName The name of the user.
     * @param password The password of the user.
     * @param domain The domain to check for the user.
     * @param callback The callback to call when the operation is finished.
     */
    public static void LoginAsync(final String userName,
            final String password,
            final String domain,
            final AsyncQuery callback) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        LoginAsync(userName, password, domain, callback, service);
    }

    /**
     * ASynchronous user login.
     * @param userName The name of the user.
     * @param password The password of the user.
     * @param domain The domain to check for the user.
     * @param callback The callback to call when the operation is finished.
     * @param service The service to call.
     */
    static void LoginAsync(final String userName,
            final String password,
            final String domain,
            final AsyncQuery callback,
            final GenericApiGWTServiceAsync service) {
        logger.finer("Frontend: Invoking async Login."); //$NON-NLS-1$

        service.Login(userName, password, domain, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(final VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Login."); //$NON-NLS-1$
                setLoggedInUser(null);
                result.setCanDoActionMessages((ArrayList<String>) translateError(result));
                callback.getDel().onSuccess(callback.getModel(), result);
                if (getLoginHandler() != null && result.getSucceeded()) {
                    getLoginHandler().onLoginSuccess(userName, password, domain);
                }
            }

            @Override
            public void onFailure(final Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    setLoggedInUser(null);
                    callback.getDel().onSuccess(callback.getModel(), null);
                }
            }
        });
    }

    public static void LogoffAsync(VdcUser vdcUser, final AsyncQuery callback) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        LogoffAsync(vdcUser, callback, service);
    }

    static void LogoffAsync(VdcUser vdcUser, final AsyncQuery callback, final GenericApiGWTServiceAsync service) {
        logger.finer("Frontend: Invoking async Logoff."); //$NON-NLS-1$

        service.logOff(vdcUser, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(final VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Logoff."); //$NON-NLS-1$
                callback.getDel().onSuccess(callback.getModel(), result);
                if (getLoginHandler() != null) {
                    getLoginHandler().onLogout();
                }
            }

            @Override
            public void onFailure(final Throwable caught) {
                if (ignoreFailure(caught)) {
                    return;
                }
                logger.log(Level.SEVERE, "Failed to execute Logoff: " + caught, caught); //$NON-NLS-1$
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                callback.getDel().onSuccess(callback.getModel(), null);
            }
        });
    }

    /**
     * Checks if the user is logged.
     * @return {@code true} if the user is logged in, false otherwise.
     */
    public static Boolean getIsUserLoggedIn() {
        return getLoggedInUser() != null;
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
        sb.append("VdcQuery Type: '" + queryType + "', "); //$NON-NLS-1$//$NON-NLS-2$
        if (searchParameters instanceof SearchParameters) {
            SearchParameters sp = (SearchParameters) searchParameters;

            if (sp.getSearchPattern().equals("Not implemented")) { //$NON-NLS-1$
                throw new RuntimeException("Search pattern is defined as 'Not implemented', probably because of a use of String.format()"); //$NON-NLS-1$
            }

            sb.append("Type value: [" + sp.getSearchTypeValue() + "], Pattern: [" + sp.getSearchPattern() + "]"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        } else {
            sb.append("Search type is base or unknown"); //$NON-NLS-1$
        }

        logger.fine(sb.toString());
    }

    private static void dumpActionDetails(VdcActionType actionType, VdcActionParametersBase parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("actionType Type: '" + actionType + "', "); //$NON-NLS-1$//$NON-NLS-2$
        sb.append("Params: " + parameters); //$NON-NLS-1$

        logger.fine(sb.toString());
    }

    static void handleActionResult(VdcActionType actionType,
            VdcActionParametersBase parameters,
            VdcReturnValueBase result,
            IFrontendActionAsyncCallback callback,
            final Object state,
            final boolean showErrorDialog) {
        logger.log(Level.FINER, "Retrieved action result from RunAction."); //$NON-NLS-1$

        FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, result, state);
        boolean success = false;
        if (!result.getCanDoAction()) {
            result.setCanDoActionMessages((ArrayList<String>) translateError(result));
            callback.executed(f);
        } else if (showErrorDialog && result.getIsSyncronious() && !result.getSucceeded()) {
            runActionExecutionFailed(actionType, result.getFault());
            callback.executed(f);

            // Prevent another (untranslated) error message pop-up display
            // ('runActionExecutionFailed' invokes an error pop-up displaying,
            // therefore calling 'failureEventHandler' is redundant)
            success = true;
        } else {
            success = true;
            callback.executed(f);
        }

        if ((!success) && (getEventsHandler() != null)
                && (getEventsHandler().isRaiseErrorModalPanel(actionType, result.getFault()))) {
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
            // The VdcFault error property takes precedence, if it's null we try to translate the message property
            String translatedMessage = translateVdcFault(fault);
            fault.setMessage(translatedMessage);
            getEventsHandler().runActionExecutionFailed(actionType, fault);
        }
    }

    public static String translateVdcFault(VdcFault fault) {
        return getVdsmErrorsTranslator().TranslateErrorTextSingle(fault.getError() == null ? fault.getMessage() : fault.getError().toString());
    }

    public static ErrorTranslator getVdsmErrorsTranslator() {
        if (vdsmErrorsTranslator == null) {
            setVdsmErrorsTranslator(new ErrorTranslator((VdsmErrors) GWT.create(VdsmErrors.class)));
        }
        return vdsmErrorsTranslator;
    }

    protected static void setVdsmErrorsTranslator(ErrorTranslator translator) {
        vdsmErrorsTranslator = translator;
    }

    public static void Subscribe(VdcQueryType[] queryTypes)
    {
        subscribedQueryTypes = queryTypes;
    }

    public static void subscribeAdditionalQueries(VdcQueryType[] queryTypes)
    {
        if (subscribedQueryTypes == null) {
            Subscribe(queryTypes);
        }
        else {
            Set<VdcQueryType> queryTypesSet = new HashSet<VdcQueryType>();
            Collections.addAll(queryTypesSet, subscribedQueryTypes);
            Collections.addAll(queryTypesSet, queryTypes);

            VdcQueryType[] queryTypesArray = new VdcQueryType[queryTypesSet.size()];
            subscribedQueryTypes = queryTypesSet.toArray(queryTypesArray);
        }
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

    /**
     * Empty implementation of IFrontendActionAsyncCallback
     */
    private final static class NullableFrontendActionAsyncCallback implements IFrontendActionAsyncCallback {
        @Override
        public void executed(FrontendActionAsyncResult result) {
        }
    }
}

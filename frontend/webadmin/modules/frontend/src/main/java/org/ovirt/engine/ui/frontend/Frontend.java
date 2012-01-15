package org.ovirt.engine.ui.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
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
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;
import org.ovirt.engine.ui.genericapi.returnvalues.UIQueryReturnValue;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryType;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendQueryAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

public class Frontend {
    private static final String RPC_TIMEOUT_EXCEPTION_STATUS_CODE_PREFIX = "120";

    private static Logger logger = Logger.getLogger(Frontend.class.getName());
    private static IFrontendEventsHandler eventsHandler;
    private static VdcUser loggedUser;
    private static ErrorTranslator canDoActionErrorsTranslator =
            new ErrorTranslator((AppErrors) GWT.create(AppErrors.class));
    private static ErrorTranslator vdsmErrorsTranslator =
            new ErrorTranslator((VdsmErrors) GWT.create(VdsmErrors.class));

    private static VdcQueryType[] subscribedQueryTypes;

    public static EventDefinition QueryStartedEventDefinition = new EventDefinition("QueryStarted", Frontend.class);
    public static Event QueryStartedEvent = new Event(QueryStartedEventDefinition);

    public static EventDefinition QueryCompleteEventDefinition = new EventDefinition("QueryComplete", Frontend.class);
    public static Event QueryCompleteEvent = new Event(QueryCompleteEventDefinition);

    private static Event frontendFailureEvent = new Event("FrontendFailure", Frontend.class);

    private static Event frontendNotLoggedInEvent = new Event("NotLoggedIn", Frontend.class);

    public static Event getFrontendFailureEvent() {
        return frontendFailureEvent;
    }

    public static Event getFrontendNotLoggedInEvent() {
        return frontendNotLoggedInEvent;
    }

    public static void initEventsHandler(IFrontendEventsHandler eventsHandler) {
        Frontend.eventsHandler = eventsHandler;
    }

    public static IFrontendEventsHandler getEventsHandler() {
        return eventsHandler;
    }

    public static void setFrontendEventHandler(IFrontendEventsHandler eventHandler) {
        Frontend.eventsHandler = eventHandler;
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
                    "A Request to the Server failed with the following Status Code: "
                            + ((StatusCodeException) caught).getStatusCode();
        } else {
            errorMessage = "A Request to the Server failed: " + caught.getLocalizedMessage();
        }
        failureEventHandler(errorMessage);
    }

    private static void failureEventHandler(String description, String errorMessage) {
        if (errorMessage.equals("USER_IS_NOT_LOGGED_IN") || errorMessage.equals("User is not logged in.")) {
            frontendNotLoggedInEvent.raise(Frontend.class, EventArgs.Empty);
        }
        frontendFailureEvent.raise(Frontend.class, new FrontendFailureEventArgs(new Message(description, errorMessage)));
    }

    private static void failureEventHandler(ArrayList<Message> messages) {
        frontendFailureEvent.raise(Frontend.class, new FrontendFailureEventArgs(messages));
    }

    public static void RunQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final IFrontendQueryAsyncCallback callback) {
        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runQuery.");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught);
                FrontendQueryAsyncResult f = new FrontendQueryAsyncResult(queryType, parameters, null);
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                callback.OnFailure(f);
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunQuery.");
                FrontendQueryAsyncResult f = new FrontendQueryAsyncResult(queryType, parameters, result);

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString()
                            + "]");
                    if (getEventsHandler() != null) {
                        ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                        failedResult.add(result);
                        if (getEventsHandler().isRaiseErrorModalPanel(queryType))
                            failureEventHandler(result.getExceptionString());
                    }
                    callback.OnFailure(f);
                } else {
                    callback.OnSuccess(f);
                }
            }
        });
    }

    public static void RunQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final AsyncQuery callback) {
        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runQuery.");
        raiseQueryStartedEvent(queryType, callback.getContext());

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught);
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    callback.asyncCallback.OnSuccess(callback.getModel(), null);
                }
                raiseQueryCompleteEvent(queryType, callback.getContext());
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunQuery.");

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString()
                            + "]");
                    if (getEventsHandler() != null) {
                        ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                        failedResult.add(result);
                        // getEventsHandler().runQueryFailed(failedResult);
                        String errorMessage = result.getExceptionString();
                        if (errorMessage.equals("USER_IS_NOT_LOGGED_IN")
                                || errorMessage.equals("User is not logged in.")) {
                            frontendNotLoggedInEvent.raise(Frontend.class, EventArgs.Empty);
                        }
                        if (callback.isHandleFailure()) {
                            callback.getDel().OnSuccess(callback.getModel(), result);
                        }
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
        });
    }

    public static VdcQueryReturnValue RunQuery(VdcQueryType queryType, VdcQueryParametersBase parameters) {
        dumpQueryDetails(queryType, parameters);
        logger.fine("Sync RunQuery is invoked, this is not supported! replace the call with the async method!");
        return null;
    }

    public static void RunPublicQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final IFrontendQueryAsyncCallback callback) {

        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runPublicQuery.");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunPublicQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunPublicQuery: " + caught, caught);
                FrontendQueryAsyncResult f = new FrontendQueryAsyncResult(queryType, parameters, null);
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                callback.OnFailure(f);
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunPublicQuery!");
                FrontendQueryAsyncResult f = new FrontendQueryAsyncResult(queryType, parameters, result);

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString()
                            + "]");
                    if (getEventsHandler() != null) {
                        ArrayList<VdcQueryReturnValue> failedResult = new ArrayList<VdcQueryReturnValue>();
                        failedResult.add(result);
                        getEventsHandler().runQueryFailed(failedResult);
                        if (getEventsHandler().isRaiseErrorModalPanel(queryType))
                            failureEventHandler(result.getExceptionString());
                    }
                    callback.OnFailure(f);
                } else {
                    callback.OnSuccess(f);
                }
            }
        });
    }

    public static void RunPublicQuery(final VdcQueryType queryType,
            final VdcQueryParametersBase parameters,
            final AsyncQuery callback) {
        dumpQueryDetails(queryType, parameters);
        logger.finer("Frontend: Invoking async runQuery.");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunPublicQuery(queryType, parameters, new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught);
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    callback.asyncCallback.OnSuccess(callback.getModel(), null);
                }
            }

            @Override
            public void onSuccess(VdcQueryReturnValue result) {
                logger.finer("Succesful returned result from RunQuery.");

                if (!result.getSucceeded()) {
                    logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + result.getExceptionString()
                            + "]");
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
        logger.warning("Frontend: RunPublicQuery -sync- was executed, this method is not supported!");

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
        logger.finer("Frontend: Invoking async runMultipleQueries.");

        raiseQueryStartedEvent(queryTypeList, context);
        for (VdcQueryParametersBase vdcQueryParametersBase : queryParamsList) {
            vdcQueryParametersBase.setRefresh(false);
        }
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunMultipleQueries(queryTypeList, queryParamsList, new AsyncCallback<ArrayList<VdcQueryReturnValue>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunPublicQuery: " + caught, caught);
                FrontendMultipleQueryAsyncResult f =
                        new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, null);
                failureEventHandler(caught);
                callback.Executed(f);

                raiseQueryCompleteEvent(queryTypeList, context);
            }

            @Override
            public void onSuccess(ArrayList<VdcQueryReturnValue> result) {
                logger.finer("Succesful returned result from RunMultipleQueries!");
                FrontendMultipleQueryAsyncResult f =
                        new FrontendMultipleQueryAsyncResult(queryTypeList, queryParamsList, result);

                for (VdcQueryReturnValue currQRV : result) {
                    if (!currQRV.getSucceeded()) {
                        logger.log(Level.WARNING, "Failure while invoking ReturnQuery [" + currQRV.getExceptionString()
                                + "]");

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
        logger.finer("Invoking async runAction.");
        dumpActionDetails(actionType, parameters);

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.RunAction(actionType, parameters, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught);
                failureEventHandler(caught);
                FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, null, state);
                callback.Executed(f);
            }

            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!");
                handleActionResult(actionType, parameters, result, callback, state);
            }
        });
    }

    public static VdcReturnValueBase RunAction(final VdcActionType actionType, final VdcActionParametersBase parameters) {
        logger.warning("Sync RunAction is invoked, this is not supported! replace the call with the async method!");
        dumpActionDetails(actionType, parameters);

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.RunAction(actionType, parameters, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught);
                failureEventHandler(caught);
                FrontendActionAsyncResult f = new FrontendActionAsyncResult(actionType, parameters, null);
                failureEventHandler(caught);
            }

            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!");
                handleActionResult(actionType, parameters, result, null, null);
            }
        });

        return new VdcReturnValueBase();
    }

    public static void RunMultipleAction(final VdcActionType actionType,
            final ArrayList<VdcActionParametersBase> prms,
            final IFrontendMultipleActionAsyncCallback callback,
            final Object state) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.RunMultipleActions(actionType, prms, new AsyncCallback<ArrayList<VdcReturnValueBase>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught);
                failureEventHandler(caught);

                FrontendMultipleActionAsyncResult f =
                        new FrontendMultipleActionAsyncResult(actionType, prms, null, state);
                callback.Executed(f);
            }

            @Override
            public void onSuccess(ArrayList<VdcReturnValueBase> result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!");
                ArrayList<Message> messages = new ArrayList<Message>();
                translateErrors(result);
                for (VdcReturnValueBase v : result) {
                    if (!v.getCanDoAction()) {
                        messages.add(new Message(v.getDescription(),
                                getRunActionErrorMessage(v.getCanDoActionMessages())));
                    }
                }
                if (!messages.isEmpty()) {
                    failureEventHandler(messages);
                }

                FrontendMultipleActionAsyncResult f =
                        new FrontendMultipleActionAsyncResult(actionType, prms, result, state);
                callback.Executed(f);
            }
        });
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
                        if (success)
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
                            if (failureCallback != null)
                            {
                                failureCallback.Executed(result);
                            }
                        }
                    }
                }, state);
    }

    public static ArrayList<VdcReturnValueBase> RunMultipleAction(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> prms) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.RunMultipleActions(actionType, prms, new AsyncCallback<ArrayList<VdcReturnValueBase>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunAction: " + caught, caught);
                failureEventHandler(caught);
                // FrontendActionAsyncResult f = new
                // FrontendActionAsyncResult(
                // actionType, parameters, null);
            }

            @Override
            public void onSuccess(ArrayList<VdcReturnValueBase> result) {
                logger.finer("Frontend: sucessfully executed RunAction, determining result!");
                ArrayList<Message> messages = new ArrayList<Message>();
                translateErrors(result);
                for (VdcReturnValueBase v : result) {
                    if (!v.getCanDoAction()) {
                        messages.add(new Message(v.getDescription(),
                                getRunActionErrorMessage(v.getCanDoActionMessages())));
                    }
                }
                if (!messages.isEmpty()) {
                    failureEventHandler(messages);
                }
                // handleActionResult(actionType, parameters, result,
                // null);
            }
        });

        return null;
    }

    public static void UnregisterQuery(Guid asyncSearchId) {
        throw new NotImplementedException("UnregisterQuery is not implemented!");
    }

    public static Guid RegisterSearch(String searchString,
            SearchType cluster,
            int searchPageSize,
            RefObject<ObservableCollection<IVdcQueryable>> tempRefObject) {
        throw new NotImplementedException("RegisterSearch is not implemented!");
    }

    public static VdcUser Login(String entity, String entity2, String selectedItem) {
        throw new NotImplementedException("Login is not implemented!");
    }

    public static void LoginAsync(String userName, String password, String domain, final AsyncQuery callback) {
        logger.finer("Frontend: Invoking async Login.");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.Login(userName, password, domain, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Login.");
                setLoggedInUser(null);
                List<VdcReturnValueBase> failed = new ArrayList<VdcReturnValueBase>();
                failed.add(result);
                translateErrors(failed);
                callback.asyncCallback.OnSuccess(callback.getModel(), result);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute RunQuery: " + caught, caught);
                getEventsHandler().runQueryFailed(null);
                failureEventHandler(caught);
                if (callback.isHandleFailure()) {
                    setLoggedInUser(null);
                    callback.asyncCallback.OnSuccess(callback.getModel(), null);
                }
            }
        });
    }

    public static VdcReturnValueBase RunActionAsyncroniousely(VdcActionType addsanstoragedomain,
            VdcActionParametersBase param) {
        throw new NotImplementedException("RunActionAsyncroniousely is not implemented!");
    }

    public static Guid RegisterQuery(VdcQueryType getallbookmarks,
            VdcQueryParametersBase vdcQueryParametersBase,
            RefObject<ObservableCollection<IVdcQueryable>> tempRefObject) {
        throw new NotImplementedException("RegisterQuery is not implemented!");
    }

    public static void Logoff(LogoutUserParameters tempVar) {
        throw new NotImplementedException("Logoff is not implemented!");
    }

    public static void Logoff(VdcUser vdcUser, final AsyncCallback<VdcReturnValueBase> callback) {
        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.logOff(vdcUser, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute Logoff: " + caught, caught);
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Frontend: sucessfully executed logOff, determining result!");
                logger.finer("Result of logoff: succeeded? [" + result.getSucceeded() + "]");
                callback.onSuccess(result);
            }
        });
    }

    public static void LogoffAsync(VdcUser vdcUser, final AsyncQuery callback) {
        logger.finer("Frontend: Invoking async Logoff.");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();
        service.logOff(vdcUser, new AsyncCallback<VdcReturnValueBase>() {
            @Override
            public void onSuccess(VdcReturnValueBase result) {
                logger.finer("Succesful returned result from Logoff.");
                callback.asyncCallback.OnSuccess(callback.getModel(), result);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute Logoff: " + caught, caught);
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
        logger.finer("Determining whether user is logged in...");

        GenericApiGWTServiceAsync service = GenericApiGWTServiceAsync.Util.getInstance();

        service.getLoggedInUser(new AsyncCallback<VdcUser>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "Failed to execute sync getIsUserLoggedIn: " + caught, caught);
                failureEventHandler(caught);
                callback.OnFailure(null);
            }

            @Override
            public void onSuccess(VdcUser result) {
                logger.finer("Sucessfully executed sync getIsUserLoggedIn!");
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
            return "No Message";
        } else {
            return messages.iterator().next();
        }
    }

    private static void dumpQueryDetails(VdcQueryType queryType, VdcQueryParametersBase searchParameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("VdcQuery Type: '" + queryType + "', ");
        if (searchParameters instanceof SearchParameters) {
            SearchParameters sp = (SearchParameters) searchParameters;

            if (sp.getSearchPattern().equals("Not implemented")) {
                throw new RuntimeException("Search pattern is defined as 'Not implemented', probably because of a use of String.format()");
            }

            sb.append("Type value: [" + sp.getSearchTypeValue() + "], Pattern: [" + sp.getSearchPattern() + "]");
        } else {
            sb.append("Search type is base or unknown");
        }

        logger.info(sb.toString());
    }

    private static void dumpActionDetails(VdcActionType actionType, VdcActionParametersBase parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("actionType Type: '" + actionType + "', ");
        sb.append("Params: " + parameters);

        logger.info(sb.toString());
    }

    public static UIQueryReturnValue RunUIQuery(
            UIQueryType getuseractiongroups,
            UIQueryParametersBase uiQueryParametersBase) {
        // TODO Auto-generated method stub
        return null;
    }

    private static void handleActionResult(VdcActionType actionType,
            VdcActionParametersBase parameters,
            VdcReturnValueBase result,
            IFrontendActionAsyncCallback callback,
            final Object state) {
        logger.log(Level.FINER, "Retrieved action result from RunAction.");

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

        if ((!success) && (getEventsHandler() != null) && (getEventsHandler().isRaiseErrorModalPanel(actionType))) {
            String errorMessage = !result.getCanDoAction() || !result.getCanDoActionMessages().isEmpty() ?
                    getRunActionErrorMessage(result.getCanDoActionMessages()) : result.getFault().getMessage();

            failureEventHandler(result.getDescription(), errorMessage);
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
}

package org.ovirt.engine.ui.frontend.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.inject.Inject;

/**
 * This class is an implementation of the {@code CommunicationProvider} using the GWT-RPC mechanism.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class GWTRPCCommunicationProvider implements CommunicationProvider {

    /**
     * Callback interface when retrieving the GWT RPC service. We need the callback in order to retrieve the
     * XSRF token if it is not available yet.
     */
    private interface ServiceCallback {
        /**
         * The callback method with the service.
         * @param service The GWT RPC service that contains the appropriate token.
         */
        void serviceFound(GenericApiGWTServiceAsync service);
        /**
         * The failure callback in case we are unable to retrieve the approriate token.
         * @param exception The exception thrown.
         */
        void onFailure(Throwable exception);
    }

    /**
     * GWT RPC service.
     */
    private final GenericApiGWTServiceAsync service;

    /**
     * GWT XSRF service.
     */
    private final XsrfTokenServiceAsync xsrfService;

    /**
     * The XSRF request builder.
     */
    private final XsrfRpcRequestBuilder xsrfRequestBuilder;

    /**
     * Get the GWT RPC service.
     * @param callback The callback to use when determining the service.
     */
    private void getService(final ServiceCallback callback) {
        if (xsrfRequestBuilder.getXsrfToken() != null) {
            callback.serviceFound(service);
        } else {
            xsrfService.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                @Override
                public void onSuccess(XsrfToken token) {
                    xsrfRequestBuilder.setXsrfToken(token);
                    callback.serviceFound(service);
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
            });
        }
    }

    /**
     * Constructor.
     * @param asyncService GWT RPC service.
     * @param xsrfTokenService The GWT XSRF token service, used to retrieve a new XSRF token.
     */
    @Inject
    public GWTRPCCommunicationProvider(final GenericApiGWTServiceAsync asyncService,
            final XsrfTokenServiceAsync xsrfTokenService, XsrfRpcRequestBuilder xsrfRequestBuilder) {
        this.service = asyncService;
        this.xsrfService = xsrfTokenService;
        this.xsrfRequestBuilder = xsrfRequestBuilder;
    }

    /**
     * Transmit a single operation, with the expectation of a single result object.
     * @param operation The operation to execute.
     */
    void transmitOperation(final VdcOperation<?, ?> operation) {
        // Figure out if this is an action or a query.
        if (operation.isAction()) {
            // Action
            runAction(operation);
        } else {
            // Query
            if (operation.isPublic()) {
                runPublicQuery(operation);
            } else {
                runQuery(operation);
            }
        }
    }

    /**
     * Run a query that does not require the user to be logged in.
     * @param operation The operation to run.
     */
    private void runPublicQuery(final VdcOperation<?, ?> operation) {
        getService(new ServiceCallback() {
            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.runPublicQuery((VdcQueryType) operation.getOperation(),
                        (VdcQueryParametersBase) operation.getParameter(), new AsyncCallback<VdcQueryReturnValue>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        operation.getCallback().onFailure(operation, exception);
                    }

                    @Override
                    public void onSuccess(final VdcQueryReturnValue result) {
                        operation.getCallback().onSuccess(operation, result);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                operation.getCallback().onFailure(operation, exception);
            }
        });
    }

    /**
     * Run a query that requires the user to be logged in.
     * @param operation The operation to run.
     */
    private void runQuery(final VdcOperation<?, ?> operation) {
        getService(new ServiceCallback() {
            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.runQuery((VdcQueryType) operation.getOperation(),
                        (VdcQueryParametersBase) operation.getParameter(), new AsyncCallback<VdcQueryReturnValue>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        //Clear out the token, and let the retry mechanism try again.
                        xsrfRequestBuilder.setXsrfToken(null);
                        operation.getCallback().onFailure(operation, exception);
                    }

                    @Override
                    public void onSuccess(final VdcQueryReturnValue result) {
                        operation.getCallback().onSuccess(operation, result);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                operation.getCallback().onFailure(operation, exception);
            }
        });
    }

    /**
     * Run an action on the {@code GenericApiGWTServiceAsync} service.
     * @param operation The operation to run.
     */
    private void runAction(final VdcOperation<?, ?> operation) {
        getService(new ServiceCallback() {
            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.runAction((VdcActionType) operation.getOperation(),
                        (VdcActionParametersBase) operation.getParameter(), new AsyncCallback<VdcReturnValueBase>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        //Clear out the token, and let the retry mechanism try again.
                        xsrfRequestBuilder.setXsrfToken(null);
                        operation.getCallback().onFailure(operation, exception);
                    }

                    @Override
                    public void onSuccess(final VdcReturnValueBase result) {
                        operation.getCallback().onSuccess(operation, result);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                operation.getCallback().onFailure(operation, exception);
            }
        });
    }

    @Override
    public void transmitOperationList(final List<VdcOperation<?, ?>> operations) {
        // Operations can be either actions or queries. Both require different handling so lets
        // Split them out into two lists so we can process them independently.
        List<VdcOperation<?, ?>> queriesList = new ArrayList<>();
        Map<VdcActionType, List<VdcOperation<?, ?>>> actionsMap = new HashMap<>();

        for (VdcOperation<?, ?> operation: operations) {
            if (operation.isAction()) {
                List<VdcOperation<?, ?>> actionsList = actionsMap.get(operation.getOperation());
                if (actionsList == null) {
                    actionsList = new ArrayList<>();
                    actionsMap.put((VdcActionType) operation.getOperation(), actionsList);
                }
                actionsList.add(operation);
            } else {
                queriesList.add(operation);
            }
        }

        if (!actionsMap.isEmpty()) {
            // We have some actions, call method to send actions.
            transmitMultipleActions(actionsMap);
        }
        if (!queriesList.isEmpty()) {
            // We have some queries, call method to send queries.
            transmitMultipleQueries(queriesList);
        }
    }

    /**
     * Call the back-end with either RunMultipleQueries or RunQuery based on the size of the queriesList.
     * When the query(ies) complete call the appropriate callback(s).
     * @param queriesList The list of queries.
     */
    private void transmitMultipleQueries(final List<VdcOperation<?, ?>> queriesList) {
        if (queriesList.size() > 1 || (queriesList.size() == 1
                && queriesList.get(0).getCallback() instanceof VdcOperationCallbackList)) {
            final List<VdcQueryType> queryTypes = new ArrayList<>();
            final List<VdcQueryParametersBase> parameters = new ArrayList<>();

            for (VdcOperation<?, ?> operation: new ArrayList<>(queriesList)) {
                if (operation.isPublic()) {
                    queriesList.remove(operation);
                    runPublicQuery(operation);
                } else {
                    queryTypes.add((VdcQueryType) operation.getOperation());
                    parameters.add((VdcQueryParametersBase) operation.getParameter());
                }
            }

            getService(new ServiceCallback() {
                @Override
                public void serviceFound(GenericApiGWTServiceAsync service) {
                    service.runMultipleQueries((ArrayList<VdcQueryType>) queryTypes,
                            (ArrayList<VdcQueryParametersBase>) parameters,
                            new AsyncCallback<ArrayList<VdcQueryReturnValue>>() {
                        @Override
                        public void onFailure(final Throwable exception) {
                            //Clear out the token, and let the retry mechanism try again.
                            xsrfRequestBuilder.setXsrfToken(null);
                            handleMultipleQueriesFailure(queriesList, exception);
                        }

                        @Override
                        public void onSuccess(final ArrayList<VdcQueryReturnValue> result) {
                            Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackMap =
                                    getCallbackMap(queriesList);
                            for (Map.Entry<VdcOperationCallback<?, ?>,
                                    List<VdcOperation<?, ?>>> callbackEntry: callbackMap.entrySet()) {
                                List<VdcQueryReturnValue> queryResult = (List<VdcQueryReturnValue>) getOperationResult(
                                        callbackEntry.getValue(), queriesList, result);
                                if (callbackEntry.getKey() instanceof VdcOperationCallbackList) {
                                    ((VdcOperationCallbackList) callbackEntry.getKey())
                                        .onSuccess(callbackEntry.getValue(), queryResult);
                                } else {
                                    ((VdcOperationCallback) callbackEntry.getKey())
                                        .onSuccess(callbackEntry.getValue().get(0), queryResult.get(0));
                                }
                            }
                        }
                    });

                }

                @Override
                public void onFailure(Throwable exception) {
                    handleMultipleQueriesFailure(queriesList, exception);
                }
            });
        } else if (queriesList.size() == 1) {
            transmitOperation(queriesList.get(0));
        }
    }

    /**
     * Multiple queries failure handler.
     * @param queriesList The queries list.
     * @param exception The exception causing the failure.
     */
    private void handleMultipleQueriesFailure(final List<VdcOperation<?, ?>> queriesList,
            final Throwable exception) {
        //Clear out the token, and let the retry mechanism try again.
        xsrfRequestBuilder.setXsrfToken(null);
        Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackMap = getCallbackMap(queriesList);
        for (Map.Entry<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackEntry: callbackMap.entrySet()) {
            if (callbackEntry.getKey() instanceof VdcOperationCallbackList) {
                ((VdcOperationCallbackList) callbackEntry.getKey()).onFailure(callbackEntry.getValue(), exception);
            } else {
                ((VdcOperationCallback) callbackEntry.getKey()).onFailure(callbackEntry.getValue().get(0), exception);
            }
        }
    }

    /**
     * Call the back-end using either RunAction or RunMultiple actions based on the fact that the map being
     * passed is keyed on the {@code VdcActionType} this allows us to determine if there are one or more actions
     * pending per action type.
     * When the action completes call back the appropriate callback methods with the appropriate results.
     * @param actions A {@code Map} of {@code VdcActionType}s with a list of operations associated with that
     * type
     */
    private void transmitMultipleActions(final Map<VdcActionType, List<VdcOperation<?, ?>>> actions) {
        for (final Map.Entry<VdcActionType, List<VdcOperation<?, ?>>> actionEntry: actions.entrySet()) {
            List<VdcActionParametersBase> parameters = new ArrayList<>();
            final List<VdcOperation<?, ?>> allActionOperations = actionEntry.getValue();

            for (VdcOperation<?, ?> operation: allActionOperations) {
                parameters.add((VdcActionParametersBase) operation.getParameter());
            }

            if (parameters.size() > 1 || (allActionOperations.size() == 1
                    && allActionOperations.get(0).getCallback() instanceof VdcOperationCallbackList)) {
                List<VdcOperation<?, ?>> waitForResultList = getWaitForResultList(actionEntry.getValue());
                if (!waitForResultList.isEmpty()) {
                    runMultipleActions(actionEntry.getKey(), waitForResultList, parameters, allActionOperations,
                            true);
                }
                if (waitForResultList.size() != actionEntry.getValue().size()) {
                    List<VdcOperation<?, ?>> immediateReturnList = actionEntry.getValue();
                    immediateReturnList.removeAll(waitForResultList); //Don't care if it succeeds or not.
                    runMultipleActions(actionEntry.getKey(), immediateReturnList, parameters, allActionOperations,
                            false);
                }
            } else if (actionEntry.getValue().size() == 1) {
                transmitOperation(actionEntry.getValue().get(0));
            }
        }
    }

    private List<VdcOperation<?, ?>> getWaitForResultList(List<VdcOperation<?, ?>> originalList) {
        List<VdcOperation<?, ?>> result = new ArrayList<>();
        for (VdcOperation<?, ?> operation: originalList) {
            if (!operation.isFromList()) {
                result.add(operation);
            }
        }
        return result;
    }

    private void runMultipleActions(final VdcActionType actionType, final List<VdcOperation<?, ?>> operations,
            final List<VdcActionParametersBase> parameters, final List<VdcOperation<?, ?>> allActionOperations,
            final boolean waitForResults) {
        getService(new ServiceCallback() {
            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.runMultipleActions(actionType, (ArrayList<VdcActionParametersBase>) parameters,
                        false, waitForResults, new AsyncCallback<ArrayList<VdcReturnValueBase>>() {

                    @Override
                    public void onFailure(final Throwable exception) {
                        //Clear out the token, and let the retry mechanism try again.
                        xsrfRequestBuilder.setXsrfToken(null);
                        handleRunMultipleActionFailure(operations, exception);
                    }

                    @Override
                    public void onSuccess(final ArrayList<VdcReturnValueBase> result) {
                        Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackMap =
                                getCallbackMap(operations);
                        for (Map.Entry<VdcOperationCallback<?, ?>,
                                List<VdcOperation<?, ?>>> callbackEntry: callbackMap.entrySet()) {
                            List<VdcReturnValueBase> actionResult = (List<VdcReturnValueBase>)
                                    getOperationResult(callbackEntry.getValue(), allActionOperations, result);
                            if (callbackEntry.getKey() instanceof VdcOperationCallbackList) {
                                ((VdcOperationCallbackList) callbackEntry.getKey())
                                    .onSuccess(callbackEntry.getValue(), actionResult);
                            } else {
                                ((VdcOperationCallback) callbackEntry.getKey())
                                    .onSuccess(callbackEntry.getValue().get(0), actionResult.get(0));
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                handleRunMultipleActionFailure(operations, exception);
            }
        });
    }

    private void handleRunMultipleActionFailure(final List<VdcOperation<?, ?>> operations,
            final Throwable exception) {
        //Clear out the token, and let the retry mechanism try again.
        xsrfRequestBuilder.setXsrfToken(null);
        Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackMap =
                getCallbackMap(operations);
        for (Map.Entry<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackEntry: callbackMap.entrySet()) {
            if (callbackEntry.getKey() instanceof VdcOperationCallbackList) {
                ((VdcOperationCallbackList) callbackEntry.getKey()).onFailure(callbackEntry.getValue(), exception);
            } else {
                ((VdcOperationCallback) callbackEntry.getKey()).onFailure(callbackEntry.getValue().get(0),
                        exception);
            }
        }
    }

    /**
     * Map operations by callback, so we can properly call a single callback for all related operations.
     * @param operationList The list of operations to determine the map for.
     * @return A Map of operations keyed by the callback.
     */
    private Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> getCallbackMap(
            final List<VdcOperation<?, ?>> operationList) {
        Map<VdcOperationCallback<?, ?>, List<VdcOperation<?, ?>>> callbackMap = new HashMap<>();

        for (VdcOperation<?, ?> operation: operationList) {
            List<VdcOperation<?, ?>> operationsByCallback = callbackMap.get(operation.getCallback());
            if (operationsByCallback == null) {
                operationsByCallback = new ArrayList<>();
                callbackMap.put(operation.getCallback(), operationsByCallback);
            }
            operationsByCallback.add(operation);
        }

        return callbackMap;
    }

    /**
     * Build a results list that maps 1 to 1 with the operationList.
     * allOperations and allResults have the same index, so we look up the index of the operations we want in all
     * operations, then retrieve the result that matches that.
     * @param operationList The list of operations we want to compare to.
     * @param allOperations The list of all operations that we want to get indexes from.
     * @param allResults The list of results that map 1 to 1 with allOperations to get the results from.
     * @return A {@code List} of return values that maps 1 to 1 with operationList.
     */
    List<?> getOperationResult(final List<VdcOperation<?, ?>> operationList,
            final List<VdcOperation<?, ?>> allOperations, final List<?> allResults) {
        List result = new ArrayList();

        for (VdcOperation<?, ?> operation: operationList) {
            int index = allOperations.indexOf(operation);
            if (index > -1 && index < allResults.size()) {
                result.add(allResults.get(index));
            }
        }

        return result;
    }

    /**
     * Log the user out.
     * @param callback The callback to call when the operation is complete.
     */
    @Override
    public void logout(final UserCallback callback) {
        //Remove the rpc token when logging out.
        xsrfRequestBuilder.setXsrfToken(null);
        VdcReturnValueBase retVal = new VdcReturnValueBase();
        retVal.setSucceeded(true);
        callback.onSuccess(retVal);
    }

    @Override
    public void storeInHttpSession(final String key, final String value, final StorageCallback callback) {
        getService(new ServiceCallback() {

            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.storeInHttpSession(key, value, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(final Void result) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(final Throwable caught) {
                        //Clear out the token, and let the retry mechanism try again.
                        xsrfRequestBuilder.setXsrfToken(null);
                        callback.onFailure(caught);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                xsrfRequestBuilder.setXsrfToken(null);
                callback.onFailure(caught);
            }

        });
    }

    @Override
    public void retrieveFromHttpSession(final String key, final StorageCallback callback) {
        getService(new ServiceCallback() {

            @Override
            public void serviceFound(GenericApiGWTServiceAsync service) {
                service.retrieveFromHttpSession(key, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(final String result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(final Throwable caught) {
                        //Clear out the token, and let the retry mechanism try again.
                        xsrfRequestBuilder.setXsrfToken(null);
                        callback.onFailure(caught);
                    }
                });

            }

            @Override
            public void onFailure(Throwable exception) {
                xsrfRequestBuilder.setXsrfToken(null);
                callback.onFailure(exception);
            }
        });
    }
}

package org.ovirt.engine.ui.frontend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationCompleteEvent;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationStartedEvent;
import org.ovirt.engine.ui.frontend.communication.CommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.GWTRPCCommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.OperationProcessor;
import org.ovirt.engine.ui.frontend.communication.VdcOperationManager;
import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

@RunWith(MockitoJUnitRunner.class)
public class FrontendTest {

    private static final int RETRY_COUNT = 5;
    private static final String ASYNC_OPERATION_TARGET = "test"; //$NON-NLS-1$

    /**
     * Instance of the class under test.
     */
    Frontend frontend;

    /**
     * Fake GWT scheduler that can immediately call instead of deferring.
     */
    FakeGWTScheduler fakeScheduler;

    GenericApiGWTServiceAsync mockService;

    @Mock
    XsrfTokenServiceAsync mockXsrfService;
    @Mock
    ErrorTranslator mockVdsmErrorsTranslator;
    @Mock
    ErrorTranslator mockValidateErrorsTranslator;
    @Mock
    Event<EventArgs> mockFrontendNotLoggedInEvent;
    @Mock
    Event<FrontendFailureEventArgs> mockFrontendFailureEvent;
    @Mock
    IFrontendEventsHandler mockEventsHandler;
    @Mock
    UIConstants mockConstants;
    @Mock
    AsyncQuery mockAsyncQuery;
    @Mock
    IFrontendMultipleQueryAsyncCallback mockMultipleQueryCallback;
    @Mock
    INewAsyncCallback mockAsyncCallback;
    @Mock
    IAsyncConverter<Object> mockConverter;
    @Mock
    EventBus mockEventBus;
    @Mock
    XsrfRpcRequestBuilder mockXsrfRpcRequestBuilder;
    @Captor
    ArgumentCaptor<AsyncCallback<VdcQueryReturnValue>> callback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcQueryReturnValue>>> callbackMultipleQueries;

    @Before
    public void setUp() throws Exception {
        mockService = mock(GenericApiGWTServiceAsync.class, withSettings().extraInterfaces(ServiceDefTarget.class));
        fakeScheduler = new FakeGWTScheduler();
        CommunicationProvider communicationsProvider = new GWTRPCCommunicationProvider(mockService, mockXsrfService,
                mockXsrfRpcRequestBuilder);
        when(mockXsrfRpcRequestBuilder.getXsrfToken()).thenReturn(new XsrfToken("Something")); //$NON-NLS-1$
        OperationProcessor operationProcessor = new OperationProcessor(communicationsProvider);
        operationProcessor.setScheduler(fakeScheduler);
        VdcOperationManager operationsManager = new VdcOperationManager(mockEventBus, operationProcessor);
        frontend = new Frontend(operationsManager, mockValidateErrorsTranslator, mockVdsmErrorsTranslator,
                mockEventBus);
        frontend.frontendFailureEvent = mockFrontendFailureEvent;
        frontend.frontendNotLoggedInEvent = mockFrontendNotLoggedInEvent;
        frontend.setEventsHandler(mockEventsHandler);
        frontend.setConstants(mockConstants);
        when(mockAsyncQuery.getModel()).thenReturn(ASYNC_OPERATION_TARGET);
        when(mockAsyncQuery.getDel()).thenReturn(mockAsyncCallback);
    }

    @After
    public void tearDown() throws Exception {
        // Make sure that the query start has been called at least once.
        // Some of the tests might call it more than once.
        // Make sure that the action start and end have not been called.
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationStartedEvent(mockAsyncQuery.getModel()));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), true, true));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), true, false));
    }

    private void verifyAsyncQuerySucceeded() {
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), false, true));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), false, false));
    }

    private void verifyAsyncQueryFailed() {
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), false, true));
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationCompleteEvent(mockAsyncQuery.getModel(), false, false));
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_ignored_failure() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_failure_404() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verify(mockEventsHandler).runQueryFailed(null);
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is marked to handle failures, make sure the callback failure handler is called</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_failure_404_callback() {
        Object mockModel = new Object();
        when(mockAsyncQuery.isHandleFailure()).thenReturn(true);
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verify(mockAsyncCallback).onSuccess(mockModel, null);
        verify(mockEventsHandler).runQueryFailed(null);
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure only one query is called. Due to the second being a duplicate</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_failure_404_with_pending() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            // Verify that only one request is executed, until the first one is complete.
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Now finish the first request.
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again, total 3 times</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure only one query is called. Due to the second and third being a duplicate</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_failure_404_with_pending_3times() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            // Verify that only one request is executed, until the first one is complete.
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again, total 5 times</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure only one query is called. Due to the rest being a duplicate</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_failure_404_with_pending_5times() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            // Verify that only one request is executed, until the first one is complete.
            verify(mockService, times(i)).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
            // Call the failure handler.
            callback.getValue().onFailure(exception);
        }
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>The failure is NOT, a-not-logged in failure</li>
     *   <li>Return success, but the success status is !succeeded (business logic failure/not logged in)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_not_succeeded_noeventshandler_nocallbackhandler() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("Fake failure for test"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is never called, as the failure is not a USER_IS_NOT_LOGGED_IN
        verify(mockFrontendNotLoggedInEvent, never()).raise(Frontend.class, EventArgs.EMPTY);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>The failure is a not logged in failure</li>
     *   <li>Return success, but the success status is !succeeded (business logic failure/not logged in)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_not_succeeded_eventshandler_nocallbackhandler() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is called
        verify(mockFrontendNotLoggedInEvent).raise(Frontend.class, EventArgs.EMPTY);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is marked to handle failures</li>
     *   <li>The failure is a not logged in failure</li>
     *   <li>Return success, but the success status is !succeeded (business logic failure/not logged in)</li>
     *   <li>Make sure the proper model is passed to the callback failure handler</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_not_succeeded_eventshandler_callbackhandler() {
        Object mockModel = new Object();
        when(mockAsyncQuery.isHandleFailure()).thenReturn(true);
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is called
        verify(mockFrontendNotLoggedInEvent).raise(Frontend.class, EventArgs.EMPTY);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>No success converter defined</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_succeeded_eventshandler_noconverter() {
        Object mockModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncQuery).setOriginalReturnValue(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>A success converter defined</li>
     *   <li>Check that the converted value is returned to the callback</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_succeeded_eventshandler_converter() {
        Object mockModel = new Object();
        Object mockResultModel = new Object();
        Object mockConvertedModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        when(mockAsyncQuery.getConverter()).thenReturn(mockConverter);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setReturnValue(mockResultModel);
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        when(mockConverter.convert(mockResultModel, mockAsyncQuery)).thenReturn(mockConvertedModel);
        // Return value set to success
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncQuery).setOriginalReturnValue(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockConvertedModel);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a result, call the same run query again</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>No success converter defined</li>
     *   <li>Make sure that the result callback is called only once</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunQuery_success_succeeded_multiple_same_eventshandler_noconverter() {
        Object mockModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        frontend.runQuery(VdcQueryType.Search, testParameters, mockAsyncQuery, false);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to success
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with only one request, with *win* as the parameter, searching for VMs</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure (escape key pressed)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleQueries_ignored_failure() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        frontend.runMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, ASYNC_OPERATION_TARGET); //$NON-NLS-1$
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            verify(mockService, times(i)).runMultipleQueries(eq(queryTypeList), eq(queryParamsList),
                callbackMultipleQueries.capture());
            // Call the failure handler.
            callbackMultipleQueries.getValue().onFailure(exception);
        }
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), any(FrontendFailureEventArgs.class));
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter, searching for
     *    VMs</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure (escape key pressed)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleQueries_ignored_failure_multiple() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        frontend.runMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, ASYNC_OPERATION_TARGET); //$NON-NLS-1$
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            // Reset the count so we can re-add both entries again.
            fakeScheduler.resetCount();
            verify(mockService, times(i)).runMultipleQueries(eq(queryTypeList), eq(queryParamsList),
                    callbackMultipleQueries.capture());
            // Call the failure handler.
            callbackMultipleQueries.getValue().onFailure(exception);
        }
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), any(FrontendFailureEventArgs.class));
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with only multiple requests, with *win* / *lin* as the parameter,
     *   searching for VMs</li>
     *   <li>Force a failure with an HTTP status code = 404</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleQueries_404_failure() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        when(mockConstants.requestToServerFailedWithCode()).
                thenReturn("A Request to the Server failed with the following Status Code"); //$NON-NLS-1$
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        frontend.runMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, ASYNC_OPERATION_TARGET); //$NON-NLS-1$
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        // Repeat 4 times, because of retries.
        for (int i = 1; i < RETRY_COUNT; i++) {
            // Reset the count so we can re-add both entries again.
            fakeScheduler.resetCount();
            verify(mockService, times(i)).runMultipleQueries(eq(queryTypeList), eq(queryParamsList),
                    callbackMultipleQueries.capture());
            // Call the failure handler.
            callbackMultipleQueries.getValue().onFailure(exception);
        }
        ArgumentCaptor<FrontendFailureEventArgs> eventArgs = ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), eventArgs.capture());
        assertEquals("Message text didn't match", //$NON-NLS-1$
                "A Request to the Server failed with the following Status Code: 404", //$NON-NLS-1$
                eventArgs.getValue().getMessages().get(0).getText());
        verifyAsyncQueryFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter,
     *   searching for VMs</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleQueries_multiple_success() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        frontend.runMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, ASYNC_OPERATION_TARGET); //$NON-NLS-1$
        verify(mockService).runMultipleQueries(eq(queryTypeList),
                eq(queryParamsList),
                callbackMultipleQueries.capture());
        // Call the failure handler.
        List<VdcQueryReturnValue> result = new ArrayList<>();
        result.add(new VdcQueryReturnValue());
        result.get(0).setSucceeded(true);
        result.add(new VdcQueryReturnValue());
        result.get(1).setSucceeded(true);
        ArgumentCaptor<FrontendMultipleQueryAsyncResult> multipleResultCaptor =
                ArgumentCaptor.forClass(FrontendMultipleQueryAsyncResult.class);
        callbackMultipleQueries.getValue().onSuccess((ArrayList<VdcQueryReturnValue>) result);
        verify(mockMultipleQueryCallback).executed(multipleResultCaptor.capture());
        assertEquals("callback result much match", result, //$NON-NLS-1$
                multipleResultCaptor.getValue().getReturnValues());
        verifyAsyncQuerySucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter,
     *   searching for VMs</li>
     *   <li>Return success, the success status is succeeded, with a failure in the result set</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleQueries_multiple_success_and_failure() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        frontend.runMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, ASYNC_OPERATION_TARGET); //$NON-NLS-1$
        verify(mockService).runMultipleQueries(eq(queryTypeList), eq(queryParamsList),
                callbackMultipleQueries.capture());
        // Call the failure handler.
        List<VdcQueryReturnValue> result = new ArrayList<>();
        result.add(new VdcQueryReturnValue());
        result.get(0).setSucceeded(false);
        result.add(new VdcQueryReturnValue());
        result.get(1).setSucceeded(true);
        ArgumentCaptor<FrontendMultipleQueryAsyncResult> multipleResultCaptor =
                ArgumentCaptor.forClass(FrontendMultipleQueryAsyncResult.class);
        callbackMultipleQueries.getValue().onSuccess((ArrayList<VdcQueryReturnValue>) result);
        verify(mockMultipleQueryCallback).executed(multipleResultCaptor.capture());
        assertEquals("callback result much match", result, //$NON-NLS-1$
                multipleResultCaptor.getValue().getReturnValues());
        verifyAsyncQuerySucceeded();
    }

}

package org.ovirt.engine.ui.frontend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

@RunWith(MockitoJUnitRunner.class)
public class FrontendTest {

    @Mock
    GenericApiGWTServiceAsync mockService;
    @Mock
    ErrorTranslator mockVdsmErrorsTranslator;
    @Mock
    ErrorTranslator mockCanDoActionErrorsTranslator;
    @Mock
    Event queryCompleteEvent;
    @Mock
    Event queryStartEvent;
    @Mock
    Event mockFrontendNotLoggedInEvent;
    @Mock
    Event mockFrontendFailureEvent;
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
    IAsyncConverter mockConverter;
    @Captor
    ArgumentCaptor<AsyncCallback<VdcQueryReturnValue>> callback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcQueryReturnValue>>> callbackMultipleQueries;

    @Before
    public void setUp() throws Exception {
        Frontend.setAppErrorsTranslator(mockCanDoActionErrorsTranslator);
        Frontend.setVdsmErrorsTranslator(mockVdsmErrorsTranslator);
        Frontend.QueryCompleteEvent = queryCompleteEvent;
        Frontend.QueryStartedEvent = queryStartEvent;
        Frontend.frontendFailureEvent = mockFrontendFailureEvent;
        Frontend.frontendNotLoggedInEvent = mockFrontendNotLoggedInEvent;
        Frontend.Subscribe(new VdcQueryType[] { VdcQueryType.Search });
        Frontend.setEventsHandler(mockEventsHandler);
        Frontend.constants = mockConstants;
        when(mockAsyncQuery.getContext()).thenReturn("test"); //$NON-NLS-1$
        when(mockAsyncQuery.getDel()).thenReturn(mockAsyncCallback);
    }

    @After
    public void tearDown() throws Exception {
        // Make sure that the query start and end have been called at least once.
        // Some of the tests might call it more than once.
        verify(queryStartEvent, atLeastOnce()).raise(Frontend.class, EventArgs.Empty);
        verify(queryCompleteEvent, atLeastOnce()).raise(Frontend.class, EventArgs.Empty);

        // Make sure the context is correct
        assertEquals("Context should be 'test'", Frontend.getCurrentContext(), "test"); //$NON-NLS-1$ //$NON-NLS-2$
        // Make sure that the pending requests and current requests are empty.
        assertEquals("There should be no pending requests", 0, Frontend.pendingRequests.size()); //$NON-NLS-1$
        assertEquals("There should be no current requests", 0, Frontend.currentRequests.size()); //$NON-NLS-1$
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
    public void testRunQuery_ignored_failure() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
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
    public void testRunQuery_failure_404() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
        verify(mockEventsHandler).runQueryFailed(null);
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
    public void testRunQuery_failure_404_callback() {
        Object mockModel = new Object();
        when(mockAsyncQuery.isHandleFailure()).thenReturn(true);
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
        verify(mockAsyncCallback).onSuccess(mockModel, null);
        verify(mockEventsHandler).runQueryFailed(null);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure both queries are called sequentially</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunQuery_failure_404_with_pending() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        // Verify that only one request is executed, until the first one is complete.
        verify(mockService, times(1)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Now finish the first request.
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
        // Verify that the second one is called since the first one is now complete.
        verify(mockService, times(2)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Call the failure handler for the second request.
        callback.getValue().onFailure(exception);
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again, total 3 times</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure both queries are called sequentially</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunQuery_failure_404_with_pending_3times() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        // Verify that only one request is executed, until the first one is complete.
        verify(mockService, times(1)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Now finish the first request.
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
        // Verify that the second one is called since the first one is now complete.
        verify(mockService, times(2)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Call the failure handler for the second request.
        callback.getValue().onFailure(exception);
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a failure result, call the same run query again, total 5 times</li>
     *   <li>The callback is NOT marked to handle failures</li>
     *   <li>Force a failure with an HTTP status code = 404 (file not found)</li>
     *   <li>Check to make sure both queries are called sequentially</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunQuery_failure_404_with_pending_5times() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        // Verify that only one request is executed, until the first one is complete.
        verify(mockService, times(1)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Now finish the first request.
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callback.getValue().onFailure(exception);
        // Verify that the second one is called since the first one is now complete.
        verify(mockService, times(2)).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        // Call the failure handler for the second request.
        callback.getValue().onFailure(exception);
        verify(mockEventsHandler, atLeastOnce()).runQueryFailed(null);
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
    public void testRunQuery_success_not_succeeded_noeventshandler_nocallbackhandler() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("Fake failure for test"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is never called, as the failure is not a USER_IS_NOT_LOGGED_IN
        verify(mockFrontendNotLoggedInEvent, never()).raise(Frontend.class, EventArgs.Empty);
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
    public void testRunQuery_success_not_succeeded_eventshandler_nocallbackhandler() {
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is called
        verify(mockFrontendNotLoggedInEvent).raise(Frontend.class, EventArgs.Empty);
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
    public void testRunQuery_success_not_succeeded_eventshandler_callbackhandler() {
        Object mockModel = new Object();
        when(mockAsyncQuery.isHandleFailure()).thenReturn(true);
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to failure
        mockReturnValue.setSucceeded(false);
        callback.getValue().onSuccess(mockReturnValue);
        // Make sure the not logged in event is called
        verify(mockFrontendNotLoggedInEvent).raise(Frontend.class, EventArgs.Empty);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
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
    public void testRunQuery_success_succeeded_eventshandler_noconverter() {
        Object mockModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncQuery).setOriginalReturnValue(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
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
    public void testRunQuery_success_succeeded_eventshandler_converter() {
        Object mockModel = new Object();
        Object mockResultModel = new Object();
        Object mockConvertedModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        when(mockAsyncQuery.getConverter()).thenReturn(mockConverter);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setReturnValue(mockResultModel);
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        when(mockConverter.Convert(mockResultModel, mockAsyncQuery)).thenReturn(mockConvertedModel);
        // Return value set to success
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncQuery).setOriginalReturnValue(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockConvertedModel);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a search query, with *win* as the parameter, searching for VMs</li>
     *   <li>Immediately, before returning a result, call the same run query again</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>No success converter defined</li>
     *   <li>Make sure that the result callback is called (once normally, once pending)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunQuery_success_succeeded_multiple_same_eventshandler_noconverter() {
        Object mockModel = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(mockModel);
        VdcQueryParametersBase testParameters = new SearchParameters("*win*", SearchType.VM); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        Frontend.RunQuery(VdcQueryType.Search, testParameters, mockService, mockAsyncQuery);
        verify(mockService).RunQuery(eq(VdcQueryType.Search), eq(testParameters), callback.capture());
        VdcQueryReturnValue mockReturnValue = new VdcQueryReturnValue();
        mockReturnValue.setExceptionString("USER_IS_NOT_LOGGED_IN"); //$NON-NLS-1$
        // Return value set to success
        mockReturnValue.setSucceeded(true);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
        reset(mockAsyncCallback);
        callback.getValue().onSuccess(mockReturnValue);
        verify(mockAsyncCallback).onSuccess(mockModel, mockReturnValue);
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
    public void testRunMultipleQueries_ignored_failure() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, "test", //$NON-NLS-1$
                mockService);
        verify(mockService).RunMultipleQueries(eq(queryTypeList),
                eq(queryParamsList),
                callbackMultipleQueries.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callbackMultipleQueries.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), any(FrontendFailureEventArgs.class));
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter, searching for VMs</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure (escape key pressed)</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunMultipleQueries_ignored_failure_multiple() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, "test", //$NON-NLS-1$
                mockService);
        verify(mockService).RunMultipleQueries(eq(queryTypeList),
                eq(queryParamsList),
                callbackMultipleQueries.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callbackMultipleQueries.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), any(FrontendFailureEventArgs.class));
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
    public void testRunMultipleQueries_404_failure() {
        when(mockConstants.requestToServerFailedWithCode()).
                thenReturn("A Request to the Server failed with the following Status Code"); //$NON-NLS-1$
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, "test", //$NON-NLS-1$
                mockService);
        verify(mockService).RunMultipleQueries(eq(queryTypeList),
                eq(queryParamsList),
                callbackMultipleQueries.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        // Call the failure handler.
        callbackMultipleQueries.getValue().onFailure(exception);
        ArgumentCaptor<FrontendFailureEventArgs> eventArgs = ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), eventArgs.capture());
        assertEquals("Message text didn't match", //$NON-NLS-1$
                "A Request to the Server failed with the following Status Code: 404", //$NON-NLS-1$
                eventArgs.getValue().getMessage().getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter, searching for VMs</li>
     *   <li>Return success, the success status is succeeded</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunMultipleQueries_multiple_success() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, "test", //$NON-NLS-1$
                mockService);
        verify(mockService).RunMultipleQueries(eq(queryTypeList),
                eq(queryParamsList),
                callbackMultipleQueries.capture());
        // Call the failure handler.
        List<VdcQueryReturnValue> result = new ArrayList<VdcQueryReturnValue>();
        result.add(new VdcQueryReturnValue());
        result.get(0).setSucceeded(true);
        result.add(new VdcQueryReturnValue());
        result.get(1).setSucceeded(true);
        ArgumentCaptor<FrontendMultipleQueryAsyncResult> multipleResultCaptor =
                ArgumentCaptor.forClass(FrontendMultipleQueryAsyncResult.class);
        callbackMultipleQueries.getValue().onSuccess((ArrayList<VdcQueryReturnValue>) result);
        verify(mockMultipleQueryCallback).executed(multipleResultCaptor.capture());
        assertEquals("callback result much match", result, multipleResultCaptor.getValue().getReturnValues()); //$NON-NLS-1$
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple search query, with multiple requests, with *win* or *lin* as the parameter, searching for VMs</li>
     *   <li>Return success, the success status is succeeded, with a failure in the result set</li>
     *   <li>Check to make sure the appropriate query start and query complete events are fired</li>
     * </ol>
     */
    @Test
    public void testRunMultipleQueries_multiple_success_and_failure() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.Search);
        queryTypeList.add(VdcQueryType.Search);
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();
        queryParamsList.add(new SearchParameters("*win*", SearchType.VM)); //$NON-NLS-1$
        queryParamsList.add(new SearchParameters("*lin*", SearchType.VM)); //$NON-NLS-1$
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, mockMultipleQueryCallback, "test", //$NON-NLS-1$
                mockService);
        verify(mockService).RunMultipleQueries(eq(queryTypeList), eq(queryParamsList),
                callbackMultipleQueries.capture());
        // Call the failure handler.
        List<VdcQueryReturnValue> result = new ArrayList<VdcQueryReturnValue>();
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
    }

}

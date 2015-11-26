package org.ovirt.engine.ui.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import java.util.Arrays;
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
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationCompleteEvent;
import org.ovirt.engine.ui.frontend.communication.AsyncOperationStartedEvent;
import org.ovirt.engine.ui.frontend.communication.CommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.GWTRPCCommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.OperationProcessor;
import org.ovirt.engine.ui.frontend.communication.VdcOperationManager;
import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * Do the actions in a separate unit test as the environment testing is different between queries and
 * actions
 */
@RunWith(MockitoJUnitRunner.class)
public class FrontendActionTest {

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
    IFrontendMultipleActionAsyncCallback mockMultipleActionCallback;
    @Mock
    IFrontendActionAsyncCallback mockActionCallback;
    @Mock
    IFrontendActionAsyncCallback mockActionFailureCallback;
    @Mock
    IFrontendEventsHandler mockEventsHandler;
    @Mock
    Event<FrontendFailureEventArgs> mockFrontendFailureEvent;
    @Mock
    UIConstants mockConstants;
    @Mock
    AsyncQuery mockAsyncQuery;
    @Mock
    INewAsyncCallback mockAsyncCallback;
    @Mock
    EventBus mockEventBus;
    @Mock
    XsrfRpcRequestBuilder mockXsrfRpcRequestBuilder;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcReturnValueBase>>> callbackMultipleActions;
    @Captor
    ArgumentCaptor<AsyncCallback<VdcReturnValueBase>> callbackAction;
    @Captor
    ArgumentCaptor<FrontendMultipleActionAsyncResult> callbackMultipleParam;
    @Captor
    ArgumentCaptor<FrontendActionAsyncResult> callbackParam;

    private static String NO_MESSAGE = "No Message"; //$NON-NLS-1$

    private Object testState = new Object();

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
        frontend.setEventsHandler(mockEventsHandler);
        frontend.setConstants(mockConstants);
        frontend.frontendFailureEvent = mockFrontendFailureEvent;
        when(mockAsyncQuery.getDel()).thenReturn(mockAsyncCallback);
        when(mockConstants.noValidateMessage()).thenReturn(NO_MESSAGE);
    }

    @After
    public void tearDown() throws Exception {
        // Make sure that the query start and end have not been called.
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, false, true));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, false, false));
    }

    private void verifyAsyncActionStarted() {
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationStartedEvent(testState));
    }

    private void verifyAsyncActionStartedAndSucceeded() {
        verifyAsyncActionStarted();
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationCompleteEvent(testState, true, true));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, true, false));
    }

    private void verifyAsyncActionStartedAndFailed() {
        verifyAsyncActionStarted();
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, true, true));
        verify(mockEventBus, atLeastOnce()).fireEvent(new AsyncOperationCompleteEvent(testState, true, false));
    }

    private void verifyAsyncActionStartedButNotCompleted() {
        verifyAsyncActionStarted();
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, true, true));
        verify(mockEventBus, never()).fireEvent(new AsyncOperationCompleteEvent(testState, true, false));
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple actions, a single action</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure (escape key pressed)</li>
     *   <li>Check to make sure the failure event is never fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_ignored_failure_multiple() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new VdcActionParametersBase());
        testState = null;
        frontend.runMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackMultipleActions.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback, never()).executed(callbackMultipleParam.capture());
        verifyAsyncActionStartedButNotCompleted();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple actions, a single action</li>
     *   <li>Force a failure with an HTTP status code = 404</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the proper values are passed to the callback.</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_404_failure_multiple() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new VdcActionParametersBase());
        frontend.runMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        callbackMultipleActions.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("ActionType should be 'AddLocalStorageDomain'", VdcActionType.AddLocalStorageDomain, //$NON-NLS-1$
                callbackMultipleParam.getValue().getActionType());
        assertEquals("Parameters should match", parameters, //$NON-NLS-1$
                callbackMultipleParam.getValue().getParameters());
        assertNull("There should be no result", callbackMultipleParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
        verifyAsyncActionStartedAndFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple actions, with multiple actions</li>
     *   <li>Have all of them be successful.</li>
     *   <li>Check to make sure the failure event is never fired</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActionsMultipleSuccess() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new VdcActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        parameters.add(new VdcActionParametersBase());
        frontend.runMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(true);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("Parameters should match", parameters, //$NON-NLS-1$
                callbackMultipleParam.getValue().getParameters());
        assertEquals("Result should match", returnValues, //$NON-NLS-1$
                callbackMultipleParam.getValue().getReturnValue());
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple actions, with multiple actions</li>
     *   <li>Have one of them fail. The rest is successful</li>
     *   <li>Check to make sure the failure event is never fired</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testrunMultipleActionsMultipleSuccess_oneFailure() {
        // Don't immediately call process until both queries are in the queue.
        fakeScheduler.setThreshold(2);
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        frontend.runMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(false);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> failedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockEventsHandler).runMultipleActionFailed(eq(VdcActionType.AddLocalStorageDomain),
                failedCaptor.capture());
        assertEquals("There is one failure", 1, failedCaptor.getValue().size()); //$NON-NLS-1$
        assertEquals("Failures should match", returnValues.get(1), failedCaptor.getValue().get(0)); //$NON-NLS-1$
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("Parameters should match", parameters,  //$NON-NLS-1$
                callbackMultipleParam.getValue().getParameters());
        assertEquals("Result should match", returnValues, //$NON-NLS-1$
                callbackMultipleParam.getValue().getReturnValue());
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a multiple actions, with multiple actions</li>
     *   <li>Have several of them fail. The rest is successful</li>
     *   <li>Check to make sure the failure event is never fired</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testrunMultipleActionsMultipleSuccess_multipleFailure() {
        // Don't immediately call process until all queries are in the queue.
        fakeScheduler.setThreshold(4);
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        parameters.get(1).setCommandId(Guid.EVERYONE);
        parameters.get(2).setCommandId(Guid.SYSTEM);
        frontend.runMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(false);
        returnValues.get(2).setValid(true);
        returnValues.get(3).setValid(false);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> failedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockEventsHandler).runMultipleActionFailed(eq(VdcActionType.AddLocalStorageDomain),
                failedCaptor.capture());
        assertEquals("There are two failures", 2, failedCaptor.getValue().size()); //$NON-NLS-1$
        assertEquals("Failures should match", returnValues.get(1), failedCaptor.getValue().get(0)); //$NON-NLS-1$
        assertEquals("Failures should match", returnValues.get(3), failedCaptor.getValue().get(1)); //$NON-NLS-1$
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("Parameters should match", parameters, //$NON-NLS-1$
                callbackMultipleParam.getValue().getParameters());
        assertEquals("Result should match", returnValues, //$NON-NLS-1$
                callbackMultipleParam.getValue().getReturnValue());
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Force a special failure with an HTTP status code = 0, this is an ignored failure</li>
     *   <li>Check to make sure the failure event is never fired</li>
     *   <li>Check to make sure the callback is never called</li>
     * </ol>
     */
    @Test
    public void testrunActionImpl_ignored_failure() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        frontend.runAction(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback, never()).executed(any(FrontendActionAsyncResult.class));
        verifyAsyncActionStartedButNotCompleted();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Force a failure with an HTTP status code = 404</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     */
    @Test
    public void testrunActionImpl_404_failure() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        frontend.runAction(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertNull("Result should be null", callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        verifyAsyncActionStartedAndFailed();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return success.</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     */
    @Test
    public void testrunActionImpl_success() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        frontend.runAction(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, validate=false.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, false);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, validate=true.</li>
     *   <li>Get succeeded is false.</li>
     *   <li>IsSyncronious is true.</li>
     *   <li>showDialog is true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_SucceededFalse() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(true);
        returnValue.setIsSyncronious(true);
        returnValue.setSucceeded(false); // Yes this is the default, but to make sure.
        EngineFault testFault = new EngineFault();
        returnValue.setFault(testFault);
        frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        verify(mockEventsHandler).runActionExecutionFailed(VdcActionType.AddDisk, testFault);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, validate=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_actionMessageSize_1_or_less() {
        EngineFault testFault = new EngineFault();
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("Descriptions should match", "This is a description", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getDescription());
        assertEquals("Text should match", NO_MESSAGE, //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, validate=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_withActionMessageSize1() {
        EngineFault testFault = new EngineFault();
        ArrayList<String> translatedErrors = new ArrayList<>(Arrays.asList("Translated Message 1")); //$NON-NLS-1$
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        when(mockValidateErrorsTranslator.translateErrorText(any(ArrayList.class))).thenReturn(translatedErrors);
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("Descriptions should match", "This is a description", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getDescription());
        assertEquals("Text should match translation", "Translated Message 1", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, validate=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_withActionMessageSizeGreaterThan1() {
        EngineFault testFault = new EngineFault();
        ArrayList<String> translatedErrors = new ArrayList<>(Arrays.asList(
                "Translated Message 1", "Translated Message 2")); //$NON-NLS-1$ //$NON-NLS-2$
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        when(mockValidateErrorsTranslator.translateErrorText(any(ArrayList.class))).thenReturn(translatedErrors);
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 2"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("Text should match", "Translated Message 1", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getText());
        assertEquals("Text should match", "Translated Message 2", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(1).getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run MultipleActions with a single action, that is successful.</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_1action() {
        List<VdcActionType> actionTypes = new ArrayList<>();
        actionTypes.add(VdcActionType.AddDisk);
        List<VdcActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        assertEquals("List size should be 0", 0, actionTypes.size()); //$NON-NLS-1$
        assertEquals("List size should be 0", 0, testParameters.size()); //$NON-NLS-1$
        assertEquals("List size should be 0", 0, callbacks.size()); //$NON-NLS-1$
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run MultipleActions with multiple actions, that are successful.</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_multipleaction_success_all() {
        List<VdcActionType> actionTypes = new ArrayList<>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to runAction, the size of the parameters should have decreased
        verify(mockService).runAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new VdcReturnValueBase();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback, times(2)).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run MultipleActions with multiple actions, first success, and second failure.</li>
     *   <li>Check to make sure the success callback is called for the first action</li>
     *   <li>Check to make sure the failure callback is called for the second action</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_multipleaction_success_first_success_second_failure() {
        List<VdcActionType> actionTypes = new ArrayList<>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to runAction
        verify(mockService).runAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new VdcReturnValueBase();
        returnValue.setValid(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        verifyAsyncActionStartedAndSucceeded();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run MultipleActions with multiple actions, first failure, and second success.</li>
     *   <li>Check to make sure the failure callback is called for the first action</li>
     *   <li>Make sure the success callback is never called for the second action</li>
     * </ol>
     */
    @Test
    public void testrunMultipleActions_multipleaction_success_first_failure_second_success() {
        List<VdcActionType> actionTypes = new ArrayList<>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setValid(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to runAction, the size of the parameters should have decreased
        verify(mockService, never()).runAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        verifyAsyncActionStartedAndSucceeded();
    }

}

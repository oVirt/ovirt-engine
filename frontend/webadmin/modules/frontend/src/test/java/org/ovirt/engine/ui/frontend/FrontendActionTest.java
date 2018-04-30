package org.ovirt.engine.ui.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
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
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

/**
 * Do the actions in a separate unit test as the environment testing is different between queries and
 * actions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    EventBus mockEventBus;
    @Mock
    XsrfRpcRequestBuilder mockXsrfRpcRequestBuilder;
    @Captor
    ArgumentCaptor<com.google.gwt.user.client.rpc.AsyncCallback<List<ActionReturnValue>>> callbackMultipleActions;
    @Captor
    ArgumentCaptor<com.google.gwt.user.client.rpc.AsyncCallback<ActionReturnValue>> callbackAction;
    @Captor
    ArgumentCaptor<FrontendMultipleActionAsyncResult> callbackMultipleParam;
    @Captor
    ArgumentCaptor<FrontendActionAsyncResult> callbackParam;

    private static final String NO_MESSAGE = "No Message"; //$NON-NLS-1$

    private Object testState = new Object();

    @BeforeEach
    public void setUp() {
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
        when(mockConstants.noValidateMessage()).thenReturn(NO_MESSAGE);
    }

    @AfterEach
    public void tearDown() {
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new ActionParametersBase());
        testState = null;
        frontend.runMultipleAction(ActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(ActionType.AddLocalStorageDomain), eq(parameters), eq(false),
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new ActionParametersBase());
        frontend.runMultipleAction(ActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(ActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        callbackMultipleActions.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals(ActionType.AddLocalStorageDomain, callbackMultipleParam.getValue().getActionType(),
                "ActionType should be 'AddLocalStorageDomain'"); //$NON-NLS-1$
        assertEquals(parameters, callbackMultipleParam.getValue().getParameters(),
                "Parameters should match"); //$NON-NLS-1$
        assertNull(callbackMultipleParam.getValue().getReturnValue(), "There should be no result"); //$NON-NLS-1$
        assertEquals(testState, callbackMultipleParam.getValue().getState(), "States should match"); //$NON-NLS-1$
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new ActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        parameters.add(new ActionParametersBase());
        frontend.runMultipleAction(ActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(ActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<ActionReturnValue> returnValues = new ArrayList<>();
        returnValues.add(new ActionReturnValue());
        returnValues.add(new ActionReturnValue());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(true);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals(parameters, callbackMultipleParam.getValue().getParameters(),
                "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValues, callbackMultipleParam.getValue().getReturnValue(),
                "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackMultipleParam.getValue().getState(), "States should match"); //$NON-NLS-1$
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new ActionParametersBase());
        parameters.add(new ActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        frontend.runMultipleAction(ActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(ActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<ActionReturnValue> returnValues = new ArrayList<>();
        returnValues.add(new ActionReturnValue());
        returnValues.add(new ActionReturnValue());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(false);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> failedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockEventsHandler).runMultipleActionFailed(eq(ActionType.AddLocalStorageDomain),
                failedCaptor.capture());
        assertEquals(1, failedCaptor.getValue().size(), "There is one failure"); //$NON-NLS-1$
        assertEquals(returnValues.get(1), failedCaptor.getValue().get(0), "Failures should match"); //$NON-NLS-1$
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals(parameters, callbackMultipleParam.getValue().getParameters(),
                "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValues, callbackMultipleParam.getValue().getReturnValue(),
                "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackMultipleParam.getValue().getState(), "States should match"); //$NON-NLS-1$
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
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        parameters.add(new ActionParametersBase());
        parameters.add(new ActionParametersBase());
        parameters.add(new ActionParametersBase());
        parameters.add(new ActionParametersBase());
        parameters.get(0).setCommandId(Guid.Empty);
        parameters.get(1).setCommandId(Guid.EVERYONE);
        parameters.get(2).setCommandId(Guid.SYSTEM);
        frontend.runMultipleAction(ActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState);
        verify(mockService).runMultipleActions(eq(ActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                eq(false), callbackMultipleActions.capture());
        ArrayList<ActionReturnValue> returnValues = new ArrayList<>();
        returnValues.add(new ActionReturnValue());
        returnValues.add(new ActionReturnValue());
        returnValues.add(new ActionReturnValue());
        returnValues.add(new ActionReturnValue());
        returnValues.get(0).setValid(true);
        returnValues.get(1).setValid(false);
        returnValues.get(2).setValid(true);
        returnValues.get(3).setValid(false);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> failedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockEventsHandler).runMultipleActionFailed(eq(ActionType.AddLocalStorageDomain),
                failedCaptor.capture());
        assertEquals(2, failedCaptor.getValue().size(), "There are two failures"); //$NON-NLS-1$
        assertEquals(returnValues.get(1), failedCaptor.getValue().get(0), "Failures should match"); //$NON-NLS-1$
        assertEquals(returnValues.get(3), failedCaptor.getValue().get(1), "Failures should match"); //$NON-NLS-1$
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals(parameters, callbackMultipleParam.getValue().getParameters(),
                "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValues, callbackMultipleParam.getValue().getReturnValue(),
                "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackMultipleParam.getValue().getState(), "States should match"); //$NON-NLS-1$
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
        ActionParametersBase testParameters = new ActionParametersBase();
        frontend.runAction(ActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback, never()).executed(any());
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
        ActionParametersBase testParameters = new ActionParametersBase();
        frontend.runAction(ActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND,
                "404 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertNull(callbackParam.getValue().getReturnValue(), "Result should be null"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
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
        ActionParametersBase testParameters = new ActionParametersBase();
        frontend.runAction(ActionType.AddDisk, testParameters, mockActionCallback, testState, false);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters), callbackAction.capture());
        ActionReturnValue returnValue = new ActionReturnValue();
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
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
        ActionParametersBase testParameters = new ActionParametersBase();
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(ActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, false);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
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
        ActionParametersBase testParameters = new ActionParametersBase();
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(true);
        returnValue.setIsSynchronous(true);
        returnValue.setSucceeded(false); // Yes this is the default, but to make sure.
        EngineFault testFault = new EngineFault();
        returnValue.setFault(testFault);
        frontend.handleActionResult(ActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
        verify(mockEventsHandler).runActionExecutionFailed(ActionType.AddDisk, testFault);
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
        when(mockEventsHandler.isRaiseErrorModalPanel(ActionType.AddDisk, testFault)).thenReturn(true);
        ActionParametersBase testParameters = new ActionParametersBase();
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(ActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("This is a description", failureCaptor.getValue().getMessages().get(0).getDescription(), //$NON-NLS-1$
                "Descriptions should match"); //$NON-NLS-1$
        assertEquals(NO_MESSAGE, failureCaptor.getValue().getMessages().get(0).getText(), //$NON-NLS-1$
                "Text should match"); //$NON-NLS-1$
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
        ArrayList<String> translatedErrors = new ArrayList<>(Collections.singletonList("Translated Message 1")); //$NON-NLS-1$
        when(mockEventsHandler.isRaiseErrorModalPanel(ActionType.AddDisk, testFault)).thenReturn(true);
        when(mockValidateErrorsTranslator.translateErrorText(any())).thenReturn(translatedErrors);
        ActionParametersBase testParameters = new ActionParametersBase();
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(ActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("This is a description", failureCaptor.getValue().getMessages().get(0).getDescription(), //$NON-NLS-1$
                "Descriptions should match"); //$NON-NLS-1$
        assertEquals("Translated Message 1", failureCaptor.getValue().getMessages().get(0).getText(), //$NON-NLS-1$
                "Text should match translation"); //$NON-NLS-1$
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
        when(mockEventsHandler.isRaiseErrorModalPanel(ActionType.AddDisk, testFault)).thenReturn(true);
        when(mockValidateErrorsTranslator.translateErrorText(any())).thenReturn(translatedErrors);
        ActionParametersBase testParameters = new ActionParametersBase();
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.getValidationMessages().add("Message 2"); //$NON-NLS-1$
        returnValue.setValid(false); // Yes this is the default, but to make sure.
        frontend.handleActionResult(ActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, true);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(testParameters, callbackParam.getValue().getParameters(), "Parameters should match"); //$NON-NLS-1$
        assertEquals(returnValue, callbackParam.getValue().getReturnValue(), "Result should match"); //$NON-NLS-1$
        assertEquals(testState, callbackParam.getValue().getState(), "States should match"); //$NON-NLS-1$
        assertEquals(ActionType.AddDisk, callbackParam.getValue().getActionType(),
                "Action type should match"); //$NON-NLS-1$
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertEquals("Translated Message 1", failureCaptor.getValue().getMessages().get(0).getText(), //$NON-NLS-1$
                "Text should match"); //$NON-NLS-1$
        assertEquals("Translated Message 2", failureCaptor.getValue().getMessages().get(1).getText(), //$NON-NLS-1$
                "Text should match"); //$NON-NLS-1$
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
        List<ActionType> actionTypes = new ArrayList<>();
        actionTypes.add(ActionType.AddDisk);
        List<ActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new ActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
        assertEquals(0, actionTypes.size(), "List size should be 0"); //$NON-NLS-1$
        assertEquals(0, testParameters.size(), "List size should be 0"); //$NON-NLS-1$
        assertEquals(0, callbacks.size(), "List size should be 0"); //$NON-NLS-1$
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
        List<ActionType> actionTypes = new ArrayList<>();
        actionTypes.add(ActionType.AddDisk);
        actionTypes.add(ActionType.AddBricksToGlusterVolume);
        List<ActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new ActionParametersBase());
        testParameters.add(new ActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
        // Second call to runAction, the size of the parameters should have decreased
        verify(mockService).runAction(eq(ActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new ActionReturnValue();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback, times(2)).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
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
        List<ActionType> actionTypes = new ArrayList<>();
        actionTypes.add(ActionType.AddDisk);
        actionTypes.add(ActionType.AddBricksToGlusterVolume);
        List<ActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new ActionParametersBase());
        testParameters.add(new ActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
        // Second call to runAction
        verify(mockService).runAction(eq(ActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new ActionReturnValue();
        returnValue.setValid(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
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
        List<ActionType> actionTypes = new ArrayList<>();
        actionTypes.add(ActionType.AddDisk);
        actionTypes.add(ActionType.AddBricksToGlusterVolume);
        List<ActionParametersBase> testParameters = new ArrayList<>();
        testParameters.add(new ActionParametersBase());
        testParameters.add(new ActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        frontend.runMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setValid(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(returnValue, callbackParam.getValue().getReturnValue());
        // Second call to runAction, the size of the parameters should have decreased
        verify(mockService, never()).runAction(eq(ActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        verifyAsyncActionStartedAndSucceeded();
    }

}

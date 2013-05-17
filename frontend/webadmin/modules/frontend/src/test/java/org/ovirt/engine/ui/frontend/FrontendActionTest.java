package org.ovirt.engine.ui.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

@RunWith(MockitoJUnitRunner.class)
/**
 * Do the actions in a separate unit test as the environment testing is different between queries and
 * actions
 */
public class FrontendActionTest {
    @Mock
    GenericApiGWTServiceAsync mockService;
    @Mock
    ErrorTranslator mockVdsmErrorsTranslator;
    @Mock
    ErrorTranslator mockCanDoActionErrorsTranslator;
    @Mock
    IFrontendMultipleActionAsyncCallback mockMultipleActionCallback;
    @Mock
    IFrontendActionAsyncCallback mockActionCallback;
    @Mock
    IFrontendActionAsyncCallback mockActionFailureCallback;
    @Mock
    IFrontendEventsHandler mockEventsHandler;
    @Mock
    Event mockFrontendFailureEvent;
    @Mock
    FrontendLoginHandler mockLoginHandler;
    @Mock
    UIConstants mockConstants;
    @Mock
    AsyncQuery mockAsyncQuery;
    @Mock
    INewAsyncCallback mockAsyncCallback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcReturnValueBase>>> callbackMultipleActions;
    @Captor
    ArgumentCaptor<AsyncCallback<VdcReturnValueBase>> callbackAction;
    @Captor
    ArgumentCaptor<FrontendMultipleActionAsyncResult> callbackMultipleParam;
    @Captor
    ArgumentCaptor<FrontendActionAsyncResult> callbackParam;

    @Before
    public void setUp() throws Exception {
        Frontend.setAppErrorsTranslator(mockCanDoActionErrorsTranslator);
        Frontend.setVdsmErrorsTranslator(mockVdsmErrorsTranslator);
        Frontend.setEventsHandler(mockEventsHandler);
        Frontend.constants = mockConstants;
        Frontend.frontendFailureEvent = mockFrontendFailureEvent;
        Frontend.setLoginHandler(mockLoginHandler);
        when(mockAsyncQuery.getDel()).thenReturn(mockAsyncCallback);
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
    public void testRunMultipleActions_ignored_failure_multiple() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        parameters.add(new VdcActionParametersBase());
        Frontend.RunMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                null, mockService);
        verify(mockService).RunMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                callbackMultipleActions.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackMultipleActions.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback, never()).executed(callbackMultipleParam.capture());
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
    public void testRunMultipleActions_404_failure_multiple() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        parameters.add(new VdcActionParametersBase());
        Object testState = new Object();
        Frontend.RunMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState, mockService);
        verify(mockService).RunMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                callbackMultipleActions.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        callbackMultipleActions.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("ActionType should be 'AddLocalStorageDomain'", VdcActionType.AddLocalStorageDomain, //$NON-NLS-1$
                callbackMultipleParam.getValue().getActionType());
        assertEquals("Parameters should match", parameters, callbackMultipleParam.getValue().getParameters()); //$NON-NLS-1$
        assertNull("There should be no result", callbackMultipleParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
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
    public void testRunMultipleActionsMultipleSuccess() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        Object testState = new Object();
        Frontend.RunMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState, mockService);
        verify(mockService).RunMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<VdcReturnValueBase>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setCanDoAction(true);
        returnValues.get(1).setCanDoAction(true);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("Parameters should match", parameters, callbackMultipleParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValues, callbackMultipleParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
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
    public void testRunMultipleActionsMultipleSuccess_oneFailure() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        Object testState = new Object();
        Frontend.RunMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState, mockService);
        verify(mockService).RunMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<VdcReturnValueBase>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setCanDoAction(true);
        returnValues.get(1).setCanDoAction(false);
        callbackMultipleActions.getValue().onSuccess(returnValues);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<ArrayList> failedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockEventsHandler).runMultipleActionFailed(eq(VdcActionType.AddLocalStorageDomain),
                failedCaptor.capture());
        assertEquals("There is one failure", 1, failedCaptor.getValue().size()); //$NON-NLS-1$
        assertEquals("Failures should match", returnValues.get(1), failedCaptor.getValue().get(0)); //$NON-NLS-1$
        verify(mockMultipleActionCallback).executed(callbackMultipleParam.capture());
        assertEquals("Parameters should match", parameters, callbackMultipleParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValues, callbackMultipleParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
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
    public void testRunMultipleActionsMultipleSuccess_multipleFailure() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        parameters.add(new VdcActionParametersBase());
        Object testState = new Object();
        Frontend.RunMultipleAction(VdcActionType.AddLocalStorageDomain, parameters, false, mockMultipleActionCallback,
                testState, mockService);
        verify(mockService).RunMultipleActions(eq(VdcActionType.AddLocalStorageDomain), eq(parameters), eq(false),
                callbackMultipleActions.capture());
        ArrayList<VdcReturnValueBase> returnValues = new ArrayList<VdcReturnValueBase>();
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.add(new VdcReturnValueBase());
        returnValues.get(0).setCanDoAction(true);
        returnValues.get(1).setCanDoAction(false);
        returnValues.get(2).setCanDoAction(true);
        returnValues.get(3).setCanDoAction(false);
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
        assertEquals("Parameters should match", parameters, callbackMultipleParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValues, callbackMultipleParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackMultipleParam.getValue().getState()); //$NON-NLS-1$
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
    public void testRunActionImpl_ignored_failure() {
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        Frontend.runActionImpl(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback, never()).executed(any(FrontendActionAsyncResult.class));
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
    public void testRunActionImpl_404_failure() {
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        Frontend.runActionImpl(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (FrontendFailureEventArgs) any());
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertNull("Result should be null", callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
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
    public void testRunActionImpl_success() {
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        Frontend.runActionImpl(VdcActionType.AddDisk, testParameters, mockActionCallback, testState, false,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        callbackAction.getValue().onSuccess(returnValue);
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
     *   <li>Return logical failure, canDoAction=false.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult() {
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(false); // Yes this is the default, but to make sure.
        Frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
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
     *   <li>Return logical failure, canDoAction=true.</li>
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
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(true);
        returnValue.setIsSyncronious(true);
        returnValue.setSucceeded(false); // Yes this is the default, but to make sure.
        VdcFault testFault = new VdcFault();
        returnValue.setFault(testFault);
        Frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
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
     *   <li>Return logical failure, canDoAction=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_actionMessageSize_1_or_less() {
        VdcFault testFault = new VdcFault();
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.setCanDoAction(false); // Yes this is the default, but to make sure.
        Frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, false);
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
                failureCaptor.getValue().getMessage().getDescription());
        assertEquals("Text should match", "No Message", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessage().getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, canDoAction=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_withActionMessageSize1() {
        VdcFault testFault = new VdcFault();
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getCanDoActionMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.setCanDoAction(false); // Yes this is the default, but to make sure.
        Frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, false);
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
                failureCaptor.getValue().getMessage().getDescription());
        assertEquals("Text should match", "Message 1", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessage().getText());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run a single action</li>
     *   <li>Return logical failure, canDoAction=false.</li>
     *   <li>isRaiseErrorModalPanel returns true.</li>
     *   <li>Check to make sure the failure event is fired</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     * Test just the handler method.
     */
    @Test
    public void testHandleActionResult_isRaiseErrorModalPanel_withActionMessageSizeGreaterThan1() {
        VdcFault testFault = new VdcFault();
        when(mockEventsHandler.isRaiseErrorModalPanel(VdcActionType.AddDisk, testFault)).thenReturn(true);
        Object testState = new Object();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setFault(testFault);
        returnValue.setDescription("This is a description"); //$NON-NLS-1$
        returnValue.getCanDoActionMessages().add("Message 1"); //$NON-NLS-1$
        returnValue.getCanDoActionMessages().add("Message 2"); //$NON-NLS-1$
        returnValue.setCanDoAction(false); // Yes this is the default, but to make sure.
        Frontend.handleActionResult(VdcActionType.AddDisk, testParameters, returnValue, mockActionCallback,
                testState, false);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals("Parameters should match", testParameters, callbackParam.getValue().getParameters()); //$NON-NLS-1$
        assertEquals("Result should match", returnValue, callbackParam.getValue().getReturnValue()); //$NON-NLS-1$
        assertEquals("States should match", testState, callbackParam.getValue().getState()); //$NON-NLS-1$
        assertEquals("Action type should match", VdcActionType.AddDisk, //$NON-NLS-1$
                callbackParam.getValue().getActionType());
        ArgumentCaptor<FrontendFailureEventArgs> failureCaptor =
                ArgumentCaptor.forClass(FrontendFailureEventArgs.class);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), failureCaptor.capture());
        assertNull("Message should be null", failureCaptor.getValue().getMessage()); //$NON-NLS-1$
        assertEquals("Text should match", "Message 1", //$NON-NLS-1$ //$NON-NLS-2$
                failureCaptor.getValue().getMessages().get(0).getText());
        assertEquals("Text should match", "Message 2", //$NON-NLS-1$ //$NON-NLS-2$
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
    public void testRunMultipleActions_1action() {
        List<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        actionTypes.add(VdcActionType.AddDisk);
        List<VdcActionParametersBase> testParameters = new ArrayList<VdcActionParametersBase>();
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();
        callbacks.add(mockActionCallback);
        Object testState = new Object();
        Frontend.RunMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        assertEquals("List size should be 0", 0, actionTypes.size()); //$NON-NLS-1$
        assertEquals("List size should be 0", 0, testParameters.size()); //$NON-NLS-1$
        assertEquals("List size should be 0", 0, callbacks.size()); //$NON-NLS-1$
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Run MultipleActions with multiple actions, that are successful.</li>
     *   <li>Check to make sure the callback is called</li>
     * </ol>
     */
    @Test
    public void testRunMultipleActions_multipleaction_success_all() {
        List<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<VdcActionParametersBase>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        Object testState = new Object();
        Frontend.RunMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to RunAction, the size of the parameters should have decreased
        verify(mockService).RunAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback, times(2)).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
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
    public void testRunMultipleActions_multipleaction_success_first_success_second_failure() {
        List<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<VdcActionParametersBase>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        Object testState = new Object();
        Frontend.RunMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(true);
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to RunAction
        verify(mockService).RunAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
        returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
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
    public void testRunMultipleActions_multipleaction_success_first_failure_second_success() {
        List<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        actionTypes.add(VdcActionType.AddDisk);
        actionTypes.add(VdcActionType.AddBricksToGlusterVolume);
        List<VdcActionParametersBase> testParameters = new ArrayList<VdcActionParametersBase>();
        testParameters.add(new VdcActionParametersBase());
        testParameters.add(new VdcActionParametersBase());
        List<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();
        callbacks.add(mockActionCallback);
        callbacks.add(mockActionCallback);
        Object testState = new Object();
        Frontend.RunMultipleActions(actionTypes, testParameters, callbacks, mockActionFailureCallback, testState,
                mockService);
        verify(mockService).RunAction(eq(VdcActionType.AddDisk), eq(testParameters.get(0)), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setCanDoAction(false);
        returnValue.setSucceeded(false);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockActionFailureCallback).executed(callbackParam.capture());
        assertEquals(callbackParam.getValue().getReturnValue(), returnValue);
        // Second call to RunAction, the size of the parameters should have decreased
        verify(mockService, never()).RunAction(eq(VdcActionType.AddBricksToGlusterVolume), eq(testParameters.get(0)),
                callbackAction.capture());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to login</li>
     *   <li>Have the login fail with a status code of 0, which is an ignored failure</li>
     *   <li>Check to make sure the callback is not called</li>
     *   <li>Check to make sure the events handler is never called</li>
     *   <li>Make sure the frontend failure handler is never called</li>
     * </ol>
     */
    @Test
    public void testLoginAsync_ignored_failure() {
        String testUser = "testUser"; //$NON-NLS-1$
        String testPassword = "testpassword"; //$NON-NLS-1$
        String testDomain = "testdomain"; //$NON-NLS-1$
        Frontend.LoginAsync(testUser, testPassword, testDomain, mockAsyncQuery, mockService);
        verify(mockService).Login(eq(testUser), eq(testPassword), eq(testDomain), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (EventArgs) any());
        verify(mockEventsHandler, never()).runQueryFailed(null);
        verify(mockAsyncQuery, never()).isHandleFailure();
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to login</li>
     *   <li>Have the login fail with a status code of 404</li>
     *   <li>Check to make sure the callback is called</li>
     *   <li>Check to make sure the events handler is called</li>
     *   <li>Make sure the frontend failure handler is called</li>
     * </ol>
     */
    @Test
    public void testLoginAsync_404_failure() {
        Frontend.setLoggedInUser(new VdcUser());
        String testUser = "testUser"; //$NON-NLS-1$
        String testPassword = "testpassword"; //$NON-NLS-1$
        String testDomain = "testdomain"; //$NON-NLS-1$
        when(mockAsyncQuery.isHandleFailure()).thenReturn(Boolean.TRUE);
        Frontend.LoginAsync(testUser, testPassword, testDomain, mockAsyncQuery, mockService);
        verify(mockService).Login(eq(testUser), eq(testPassword), eq(testDomain), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockEventsHandler).runQueryFailed(null);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (EventArgs) any());
        assertNull("Logged in user should be null", Frontend.getLoggedInUser()); //$NON-NLS-1$
        verify(mockAsyncCallback).onSuccess(any(), any());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to login</li>
     *   <li>Have the login succeed</li>
     *   <li>Check to make sure the callback is called</li>
     *   <li>Check to make sure the events handler is called</li>
     *   <li>Make sure the frontend failure handler is not called</li>
     * </ol>
     */
    @Test
    public void testLoginAsync_success() {
        Object model = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(model);
        Frontend.setLoggedInUser(new VdcUser());
        String testUser = "testUser"; //$NON-NLS-1$
        String testPassword = "testpassword"; //$NON-NLS-1$
        String testDomain = "testdomain"; //$NON-NLS-1$
        when(mockAsyncQuery.isHandleFailure()).thenReturn(Boolean.TRUE);
        Frontend.LoginAsync(testUser, testPassword, testDomain, mockAsyncQuery, mockService);
        verify(mockService).Login(eq(testUser), eq(testPassword), eq(testDomain), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(true);
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockAsyncCallback).onSuccess(model, returnValue);
        verify(mockLoginHandler).onLoginSuccess(testUser, testPassword, testDomain);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (EventArgs) any());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to login</li>
     *   <li>Have the login fail</li>
     *   <li>Check to make sure the callback is called</li>
     *   <li>Check to make sure the events handler is called</li>
     *   <li>Make sure the frontend failure handler is not called</li>
     * </ol>
     */
    @Test
    public void testLoginAsync_login_failure() {
        Object model = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(model);
        Frontend.setLoggedInUser(new VdcUser());
        String testUser = "testUser"; //$NON-NLS-1$
        String testPassword = "testpassword"; //$NON-NLS-1$
        String testDomain = "testdomain"; //$NON-NLS-1$
        when(mockAsyncQuery.isHandleFailure()).thenReturn(Boolean.TRUE);
        Frontend.LoginAsync(testUser, testPassword, testDomain, mockAsyncQuery, mockService);
        verify(mockService).Login(eq(testUser), eq(testPassword), eq(testDomain), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        returnValue.setSucceeded(false); // Yes I know this is the default, just to be sure.
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockAsyncCallback).onSuccess(model, returnValue);
        verify(mockLoginHandler, never()).onLoginSuccess(testUser, testPassword, testDomain);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (EventArgs) any());
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to log-off</li>
     *   <li>Have the log-off fail with a status code of 0, which is an ignored failure</li>
     *   <li>Check to make sure the callback is not called</li>
     *   <li>Check to make sure the events handler is never called</li>
     *   <li>Make sure the onSuccess handler is never called</li>
     * </ol>
     */
    @Test
    public void testLogoffAsync_ignored_failure() {
        Object model = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(model);
        VdcUser testUser = new VdcUser();
        testUser.setUserName("testUser"); //$NON-NLS-1$
        testUser.setPassword("testpassword"); //$NON-NLS-1$
        Frontend.LogoffAsync(testUser, mockAsyncQuery, mockService);
        verify(mockService).logOff(eq(testUser), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(0, "0 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent, never()).raise(eq(Frontend.class), (EventArgs) any());
        verify(mockEventsHandler, never()).runQueryFailed(null);
        verify(mockAsyncCallback, never()).onSuccess(model, null);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to log-off</li>
     *   <li>Have the log-off fail with a status code of 404</li>
     *   <li>Check to make sure the callback is called</li>
     *   <li>Check to make sure the events handler is called</li>
     *   <li>Make sure the onSuccess handler is called</li>
     * </ol>
     */
    @Test
    public void testLogoffAsync_404_failure() {
        Object model = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(model);
        VdcUser testUser = new VdcUser();
        testUser.setUserName("testUser"); //$NON-NLS-1$
        testUser.setPassword("testpassword"); //$NON-NLS-1$
        Frontend.LogoffAsync(testUser, mockAsyncQuery, mockService);
        verify(mockService).logOff(eq(testUser), callbackAction.capture());
        StatusCodeException exception = new StatusCodeException(HttpServletResponse.SC_NOT_FOUND, "404 status code"); //$NON-NLS-1$
        callbackAction.getValue().onFailure(exception);
        verify(mockFrontendFailureEvent).raise(eq(Frontend.class), (EventArgs) any());
        verify(mockEventsHandler).runQueryFailed(null);
        verify(mockAsyncCallback).onSuccess(model, null);
    }

    /**
     * Run the following test case.
     * <ol>
     *   <li>Attempt to log-off</li>
     *   <li>Have the log-off succeed</li>
     *   <li>Make sure the onSuccess handler is called</li>
     *   <li>Make sure the onLogout handler is called</li>
     * </ol>
     */
    @Test
    public void testLogoffAsync_success() {
        Object model = new Object();
        when(mockAsyncQuery.getModel()).thenReturn(model);
        VdcUser testUser = new VdcUser();
        testUser.setUserName("testUser"); //$NON-NLS-1$
        testUser.setPassword("testpassword"); //$NON-NLS-1$
        Frontend.LogoffAsync(testUser, mockAsyncQuery, mockService);
        verify(mockService).logOff(eq(testUser), callbackAction.capture());
        VdcReturnValueBase returnValue = new VdcReturnValueBase();
        callbackAction.getValue().onSuccess(returnValue);
        verify(mockAsyncCallback).onSuccess(model, returnValue);
        verify(mockLoginHandler).onLogout();
    }
}

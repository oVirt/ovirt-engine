package org.ovirt.engine.ui.frontend.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;

@SuppressWarnings({ "unchecked", "rawtypes" })
@ExtendWith(MockitoExtension.class)
public class OperationProcessorTest {
    OperationProcessor testProcessor;

    @Mock
    EventBus gwtEventBus;
    @Mock
    CommunicationProvider mockProvider;
    @Mock
    VdcOperationManager mockOperationManager;
    @Mock
    VdcOperationCallback<VdcOperation<?, ?>, Object> mockCallback1;
    @Mock
    VdcOperationCallback<VdcOperation<?, ?>, Object> mockCallback2;
    @Mock
    VdcOperationCallback<VdcOperation<?, ?>, Object> mockCallback3;
    @Mock
    VdcOperationCallbackList<VdcOperation<?, ?>, List<ActionReturnValue>> mockCallbackList1;
    @Mock
    VdcOperationCallbackList<VdcOperation<?, ?>, List<ActionReturnValue>> mockCallbackList2;
    @Mock
    VdcOperationCallbackList<VdcOperation<?, ?>, List<ActionReturnValue>> mockCallbackList3;
    @Mock
    Scheduler mockScheduler;

    @Captor
    ArgumentCaptor<List<VdcOperation<?, ?>>> operationListCaptor;

    @BeforeEach
    public void setUp() {
        testProcessor = new OperationProcessor(mockProvider);
        testProcessor.setScheduler(mockScheduler);
    }

    @Test
    public void testOnOperationAvailableSingle() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddEventSubscription, testParameter, mockCallback1);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        // Test that we inserted the callback from the processor.
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallback1,
                "The callbacks should not match."); //$NON-NLS-1$
    }

    @Test
    public void testOnOperationAvailableList() {
        ActionParametersBase testActionParameter = new ActionParametersBase();
        QueryParametersBase testQueryParameter = new QueryParametersBase();
        VdcOperation testOperation1 = new VdcOperation(ActionType.AddEventSubscription, testActionParameter,
                mockCallback1);
        VdcOperation testOperation2 = new VdcOperation(QueryType.Search, testQueryParameter, mockCallback2);
        VdcOperation testOperation3 = new VdcOperation(ActionType.AddEmptyStoragePool, testActionParameter,
                mockCallback3);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2)
            .thenReturn(testOperation3).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertEquals(3, operationListCaptor.getValue().size(), "Should have 3 operations"); //$NON-NLS-1$
        // Test that we inserted the callback from the processor.
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(2).getCallback(), mockCallbackList3,
                "The callbacks should NOT match"); //$NON-NLS-1$
    }

    @Test
    public void testOnOperationAvailableSingle_success() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddEventSubscription, testParameter, mockCallback1);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        Object testResult = new Object();
        operationListCaptor.getValue().get(0).getCallback().onSuccess(testOperation, testResult);
        // Verify that the original callback is called.
        verify(mockCallback1).onSuccess(testOperation, testResult);
    }

    @Test
    public void testOnOperationAvailableSingle_failure_action_noretry() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddEventSubscription, testParameter, mockCallback1);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        operationListCaptor.getValue().get(0).getCallback().onFailure(testOperation, testException);
        // Verify that the original callback is called.
        verify(mockCallback1).onFailure(testOperation, testException);
    }

    @Test
    public void testOnOperationAvailableSingle_failure_with_retry() {
        QueryParametersBase testParameter = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase>testOperation =
                new VdcOperation<>(QueryType.Search, testParameter, mockCallback1);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        operationListCaptor.getValue().get(0).getCallback().onFailure(operationListCaptor.getValue().get(0),
                testException);
        // Verify that the original callback is never called.
        verify(mockCallback1, never()).onFailure(operationListCaptor.getValue().get(0), testException);
        // Verify that the operation is put back in the queue.
        verify(mockOperationManager).addOperation(operationListCaptor.getValue().get(0));
        // Verify that the callback on the operation is no longer the original.
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallback1,
                "callbacks should not match"); //$NON-NLS-1$
    }

    @Test
    public void testOnOperationAvailableSingle_failure_query_noretry() {
        QueryParametersBase testParameter = new QueryParametersBase();
        // Setup 'previous' retries, so we have exhausted the retries.
        VdcOperation<QueryType, QueryParametersBase> testOperation = new VdcOperation<>(QueryType.Search, testParameter, mockCallback1);
        testOperation = new VdcOperation(testOperation, mockCallback2);
        testOperation = new VdcOperation(testOperation, mockCallback2);
        testOperation = new VdcOperation(testOperation, mockCallback2);
        testOperation = new VdcOperation(testOperation, mockCallback2);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        operationListCaptor.getValue().get(0).getCallback().onFailure(testOperation, testException);
        // Verify that the original callback is called.
        verify(mockCallback1).onFailure(testOperation, testException);
    }

    @Test
    public void testOnOperationAvailableMultiple_success() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation testOperation1 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback1);
        VdcOperation testOperation2 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback2);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2).
            thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallback1,
                "The callbacks should not match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallback2,
                "The callbacks should not match"); //$NON-NLS-1$
        List<ActionReturnValue> resultList1 = new ArrayList<>();
        List<ActionReturnValue> resultList2 = new ArrayList<>();
        ActionReturnValue result1 = new ActionReturnValue();
        ActionReturnValue result2 = new ActionReturnValue();
        resultList1.add(result1);
        resultList2.add(result2);
        operationListCaptor.getValue().get(0).getCallback().onSuccess(operationListCaptor.getValue().get(0),
                resultList1);
        verify(mockCallback1).onSuccess(testOperation1, resultList1);
        operationListCaptor.getValue().get(1).getCallback().onSuccess(operationListCaptor.getValue().get(1),
                resultList2);
        verify(mockCallback2).onSuccess(testOperation2, resultList2);
    }

    @Test
    public void testOnOperationAvailableMultiple_success_samecallback() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation testOperation1 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback1);
        VdcOperation testOperation2 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback1);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2).
            thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        ActionReturnValue result1 = new ActionReturnValue();
        operationListCaptor.getValue().get(0).getCallback().onSuccess(operationListCaptor.getValue().get(0), result1);
        verify(mockCallback1).onSuccess(testOperation1, result1);
        verify(mockCallback2, never()).onSuccess(eq(testOperation2), any());
    }

    @Test
    public void testOnOperationAvailableMultiple_success_with_retry() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation testOperation1 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback1);
        // This is the 'retry'.
        testOperation1 = new VdcOperation(testOperation1, mockCallback3);
        VdcOperation testOperation2 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback2);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2).
            thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        List<ActionReturnValue> resultList = new ArrayList<>();
        ActionReturnValue result1 = new ActionReturnValue();
        ActionReturnValue result2 = new ActionReturnValue();
        resultList.add(result1);
        resultList.add(result2);
        operationListCaptor.getValue().get(0).getCallback().onSuccess(operationListCaptor.getValue().get(0), result1);
        operationListCaptor.getValue().get(1).getCallback().onSuccess(operationListCaptor.getValue().get(1), result2);
        verify(mockCallback1).onSuccess(testOperation1, resultList.get(0));
        verify(mockCallback2).onSuccess(testOperation2, resultList.get(1));
        verify(mockCallback3, never()).onSuccess((VdcOperation) any(), any());
    }

    @Test
    public void testOnOperationAvailableMultipleAction_failure() {
        ActionParametersBase testParameter = new ActionParametersBase();
        VdcOperation testOperation1 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback1);
        VdcOperation testOperation2 = new VdcOperation(ActionType.AddEventSubscription, testParameter,
                mockCallback2);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2).
            thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        Exception testException = new Exception("this is an exception"); //$NON-NLS-1$
        operationListCaptor.getValue().get(0).getCallback().onFailure(operationListCaptor.getValue().get(0),
                testException);
        verify(mockCallback1).onFailure(testOperation1, testException);
        operationListCaptor.getValue().get(0).getCallback().onFailure(operationListCaptor.getValue().get(1),
                testException);
        verify(mockCallback2).onFailure(testOperation2, testException);
    }

    @Test
    public void testOnOperationAvailableMultipleQuery_failure() {
        QueryParametersBase testParameter = new QueryParametersBase();
        VdcOperation testOperation1 = new VdcOperation(QueryType.GetDirectoryGroupById, testParameter,
                mockCallback1);
        VdcOperation testOperation2 = new VdcOperation(QueryType.Search, testParameter,
                mockCallback2);
        when(mockOperationManager.pollOperation()).thenReturn(testOperation1).thenReturn(testOperation2).
            thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        Exception testException = new Exception("this is an exception"); //$NON-NLS-1$
        operationListCaptor.getValue().get(0).getCallback().onFailure(operationListCaptor.getValue().get(0),
                testException);
        operationListCaptor.getValue().get(1).getCallback().onFailure(operationListCaptor.getValue().get(1),
                testException);
        verify(mockCallback1, never()).onFailure(testOperation1, testException);
        verify(mockCallback2, never()).onFailure(testOperation2, testException);
        verify(mockOperationManager).addOperation(testOperation1);
    }


    @Test
    public void testOnOperationAvailableMultiple_same_success() {
        QueryParametersBase testParameter = new QueryParametersBase();
        List<VdcOperation<?, ?>> testOperation1List = new ArrayList<>();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testParameter, mockCallbackList1);
        testOperation1List.add(testOperation1);
        VdcOperation<QueryType, QueryParametersBase> testOperation2 =
                new VdcOperation<>(QueryType.GetDirectoryGroupById, testParameter, mockCallbackList2);
        when(mockOperationManager.pollOperation()).thenReturn((VdcOperation) testOperation1).
            thenReturn((VdcOperation) testOperation1).thenReturn((VdcOperation) testOperation2).thenReturn(null);
        testProcessor.processAvailableOperations(mockOperationManager);
        verify(mockProvider).transmitOperationList(operationListCaptor.capture());
        // Check to make sure it inserted its own callback.
        assertNotEquals(operationListCaptor.getValue().get(0).getCallback(), mockCallbackList1,
                "The callbacks should NOT match"); //$NON-NLS-1$
        assertNotEquals(operationListCaptor.getValue().get(1).getCallback(), mockCallbackList2,
                "The callbacks should NOT match"); //$NON-NLS-1$
        // There should be only be two items in the list.
        assertEquals(2, operationListCaptor.getValue().size(), "There should be two items"); //$NON-NLS-1$
        List<ActionReturnValue> resultList1 = new ArrayList<>();
        List<ActionReturnValue> resultList2 = new ArrayList<>();
        ActionReturnValue result1 = new ActionReturnValue();
        ActionReturnValue result2 = new ActionReturnValue();
        resultList1.add(result1);
        resultList2.add(result2);
        List<VdcOperation> captured1List = new ArrayList<>();
        captured1List.add(operationListCaptor.getValue().get(0));
        List<VdcOperation> captured2List = new ArrayList<>();
        captured2List.add(operationListCaptor.getValue().get(1));
        operationListCaptor.getValue().get(0).getCallback().onSuccess(captured1List, resultList1);
        verify(mockCallbackList1).onSuccess(testOperation1List, resultList1);
        operationListCaptor.getValue().get(1).getCallback().onSuccess(captured2List, resultList2);
    }
}

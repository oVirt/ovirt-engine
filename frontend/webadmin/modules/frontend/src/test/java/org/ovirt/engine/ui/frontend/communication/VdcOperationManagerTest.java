package org.ovirt.engine.ui.frontend.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

import com.google.gwt.event.shared.EventBus;

@ExtendWith(MockitoExtension.class)
public class VdcOperationManagerTest {
    VdcOperationManager testManager;

    @Mock
    OperationProcessor mockOperationProcessor;

    @Mock
    EventBus mockEventBus;

    @BeforeEach
    public void setUp() {
        testManager = new VdcOperationManager(mockEventBus, mockOperationProcessor);
    }

    @Test
    public void testAddOperationAction() {
        VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddNetworkOnProvider, new ActionParametersBase(), null);
        testManager.addOperation(testOperation);
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any());
        assertEquals(testOperation, testManager.pollOperation(), "Operations must match"); //$NON-NLS-1$
    }

    @Test
    public void testAddOperationMultipleQuery() {
        VdcOperation<QueryType, QueryParametersBase> testOperation = new VdcOperation<>(QueryType.Search, new QueryParametersBase().withRefresh(), null);
        testManager.addOperation(testOperation);
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any());
        // Second add, shouldn't add and generate an event.
        testManager.addOperation(testOperation);
        // Verify it is only called once (from before)
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any());
    }

    @Test
    public void testAddOperationList() {
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.AddNetworkOnProvider, new ActionParametersBase(), null);
        QueryParametersBase testParameters = new QueryParametersBase().withRefresh();
        VdcOperation<QueryType, QueryParametersBase> testOperation2 = new VdcOperation<>(QueryType.Search, testParameters, null);
        VdcOperation<QueryType, QueryParametersBase> testOperation3 = new VdcOperation<>(QueryType.Search, testParameters, null);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        operationList.add(testOperation3);
        testManager.addOperationList(operationList);
        verify(mockOperationProcessor, times(3)).processOperation(testManager);
        verify(mockEventBus, times(2)).fireEvent(any());
        assertEquals(testManager.pollOperation(), testOperation1, "First poll should be action"); //$NON-NLS-1$
        assertEquals(testManager.pollOperation(), testOperation2, "Second poll should be query"); //$NON-NLS-1$
        assertNull(testManager.pollOperation(), "Third poll should be null"); //$NON-NLS-1$
    }

}

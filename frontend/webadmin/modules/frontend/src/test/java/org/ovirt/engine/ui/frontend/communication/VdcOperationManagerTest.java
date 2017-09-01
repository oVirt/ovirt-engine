package org.ovirt.engine.ui.frontend.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

import com.google.gwt.event.shared.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class VdcOperationManagerTest {
    VdcOperationManager testManager;

    @Mock
    OperationProcessor mockOperationProcessor;

    @Mock
    EventBus mockEventBus;

    @Before
    public void setUp() throws Exception {
        testManager = new VdcOperationManager(mockEventBus, mockOperationProcessor);
    }

    @Test
    public void testAddOperationAction() {
        VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddNetworkOnProvider, new ActionParametersBase(), null);
        testManager.addOperation(testOperation);
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any());
        assertEquals("Operations must match", testOperation, testManager.pollOperation()); //$NON-NLS-1$
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
        assertEquals("First poll should be action", testManager.pollOperation(), testOperation1); //$NON-NLS-1$
        assertEquals("Second poll should be query", testManager.pollOperation(), testOperation2); //$NON-NLS-1$
        assertNull("Third poll should be null", testManager.pollOperation()); //$NON-NLS-1$
    }

}

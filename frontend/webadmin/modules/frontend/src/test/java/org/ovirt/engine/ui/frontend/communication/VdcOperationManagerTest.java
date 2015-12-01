package org.ovirt.engine.ui.frontend.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import com.google.gwt.event.shared.EventBus;

@RunWith(MockitoJUnitRunner.class)
public class VdcOperationManagerTest {
    VdcOperationManager testManager;

    @Mock
    OperationProcessor mockOperationProcessor;

    @Mock
    EventBus mockEventBus;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        testManager = new VdcOperationManager(mockEventBus, mockOperationProcessor);
    }

    @Test
    public void testAddOperationAction() {
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation =
                new VdcOperation<>(VdcActionType.AddNetworkOnProvider, new VdcActionParametersBase(), null);
        testManager.addOperation(testOperation);
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any(EngineSessionRefreshedEvent.class));
        assertEquals("Operations must match", testOperation, testManager.pollOperation()); //$NON-NLS-1$
    }

    @Test
    public void testAddOperationMultipleQuery() {
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation = new VdcOperation<>(VdcQueryType.Search, new VdcQueryParametersBase(), null);
        testManager.addOperation(testOperation);
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any(EngineSessionRefreshedEvent.class));
        // Second add, shouldn't add and generate an event.
        testManager.addOperation(testOperation);
        // Verify it is only called once (from before)
        verify(mockOperationProcessor).processOperation(testManager);
        verify(mockEventBus).fireEvent(any(EngineSessionRefreshedEvent.class));
    }

    @Test
    public void testAddOperationList() {
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.AddNetworkOnProvider, new VdcActionParametersBase(), null);
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation2 = new VdcOperation<>(VdcQueryType.Search, testParameters, null);
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation3 = new VdcOperation<>(VdcQueryType.Search, testParameters, null);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        operationList.add(testOperation3);
        testManager.addOperationList(operationList);
        verify(mockOperationProcessor, times(3)).processOperation(testManager);
        verify(mockEventBus, times(2)).fireEvent(any(EngineSessionRefreshedEvent.class));
        assertEquals("First poll should be action", testManager.pollOperation(), testOperation1); //$NON-NLS-1$
        assertEquals("Second poll should be query", testManager.pollOperation(), testOperation2); //$NON-NLS-1$
        assertNull("Third poll should be null", testManager.pollOperation()); //$NON-NLS-1$
    }

}

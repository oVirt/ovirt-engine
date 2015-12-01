package org.ovirt.engine.ui.frontend.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

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
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

@RunWith(MockitoJUnitRunner.class)
public class GWTRPCCommunicationProviderTest {

    @Mock
    GenericApiGWTServiceAsync mockService;
    @Mock
    XsrfTokenServiceAsync mockXsrfService;
    @Mock
    VdcOperationCallback mockOperationCallbackSingle1;
    @Mock
    VdcOperationCallback mockOperationCallbackSingle2;
    @Mock
    VdcOperationCallbackList mockOperationCallbackList1;
    @Mock
    VdcOperationCallbackList mockOperationCallbackList2;
    @Mock
    EventBus mockEventBus;

    XsrfRpcRequestBuilder mockXsrfRpcRequestBuilder;

    @Captor
    ArgumentCaptor<AsyncCallback<VdcReturnValueBase>> actionCallback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcReturnValueBase>>> actionCallbackList;
    @Captor
    ArgumentCaptor<AsyncCallback<VdcQueryReturnValue>> queryCallback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<VdcQueryReturnValue>>> queryCallbackList;

    /**
     * The provider under test.
     */
    GWTRPCCommunicationProvider testProvider;

    @Before
    public void setUp() throws Exception {
        mockXsrfRpcRequestBuilder = new XsrfRpcRequestBuilder();
        testProvider = new GWTRPCCommunicationProvider(mockService, mockXsrfService, mockXsrfRpcRequestBuilder);
        mockXsrfRpcRequestBuilder.setXsrfToken(new XsrfToken("Something")); //$NON-NLS-1$
    }

    @Test
    public void testTransmitOperationAction_success() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        final List<VdcOperation<VdcActionType, VdcActionParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<VdcActionType, VdcActionParametersBase> testOperation =
                new VdcOperation<>(VdcActionType.AddDisk, testParameters,
                new VdcOperationCallback<VdcOperation<VdcActionType, VdcActionParametersBase>, VdcReturnValueBase>() {

            @Override
            public void onSuccess(VdcOperation<VdcActionType, VdcActionParametersBase> operation,
                    VdcReturnValueBase result) {
                assertEquals("Test results should match", testResult, result); //$NON-NLS-1$
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<VdcActionType, VdcActionParametersBase> operation, Throwable caught) {
                fail("Should not get here"); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onSuccess(testResult);
    }

    @Test
    public void testTransmitOperationAction_failure() {
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        final List<VdcOperation<VdcActionType, VdcActionParametersBase>> operationList = new ArrayList<>();
        final Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        final VdcOperation<VdcActionType, VdcActionParametersBase> testOperation =
                new VdcOperation<>(VdcActionType.AddDisk, testParameters,
                new VdcOperationCallback<VdcOperation<VdcActionType, VdcActionParametersBase>, VdcReturnValueBase>() {

            @Override
            public void onSuccess(VdcOperation<VdcActionType, VdcActionParametersBase> operation,
                    VdcReturnValueBase result) {
                fail("Should not get here"); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<VdcActionType, VdcActionParametersBase> operation, Throwable exception) {
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
                assertEquals("Exceptions should match", testException, exception); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runAction(eq(VdcActionType.AddDisk), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onFailure(testException);
    }

    @Test
    public void testTransmitOperationQuery_success() {
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        final VdcQueryReturnValue testResult = new VdcQueryReturnValue();
        final List<VdcOperation<VdcQueryType, VdcQueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation =
                new VdcOperation<>(VdcQueryType.Search, testParameters,
                new VdcOperationCallback<VdcOperation<VdcQueryType, VdcQueryParametersBase>, VdcQueryReturnValue>() {

            @Override
            public void onSuccess(VdcOperation<VdcQueryType, VdcQueryParametersBase> operation,
                    VdcQueryReturnValue result) {
                assertEquals("Test results should match", testResult, result); //$NON-NLS-1$
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<VdcQueryType, VdcQueryParametersBase> operation, Throwable caught) {
                fail("Should not get here"); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), queryCallback.capture());
        queryCallback.getValue().onSuccess(testResult);
    }

    @Test
    public void testTransmitOperationQuery_failure() {
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        final Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        final List<VdcOperation<VdcQueryType, VdcQueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation =
                new VdcOperation<>(VdcQueryType.Search, testParameters,
                new VdcOperationCallback<VdcOperation<VdcQueryType, VdcQueryParametersBase>, VdcQueryReturnValue>() {

            @Override
            public void onSuccess(VdcOperation<VdcQueryType, VdcQueryParametersBase> operation,
                    VdcQueryReturnValue result) {
                fail("Should not get here"); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<VdcQueryType, VdcQueryParametersBase> operation, Throwable exception) {
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
                assertEquals("Exceptions should match", testException, exception); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), queryCallback.capture());
        queryCallback.getValue().onFailure(testException);
    }

    @Test
    public void testGetOperationResult_Empty() {
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        List<?> allResults = new ArrayList<>();
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have no results", 0, result.size()); //$NON-NLS-1$
    }

    @Test
    public void testGetOperationResult_One() {
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, new VdcActionParametersBase(), null);
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        testOperationList.add(testOperation1);
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        allOperationList.add(testOperation1);
        VdcReturnValueBase testResult1 = new VdcReturnValueBase();
        List<VdcReturnValueBase> allResults = new ArrayList<>();
        allResults.add(testResult1);
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have one results", 1, result.size()); //$NON-NLS-1$
    }

    @Test
    public void testGetOperationResult_One_of_Two() {
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, new VdcActionParametersBase(), null);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.AddBookmark, new VdcActionParametersBase(), null);
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        testOperationList.add(testOperation2);
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        allOperationList.add(testOperation1);
        allOperationList.add(testOperation2);
        VdcReturnValueBase testResult1 = new VdcReturnValueBase();
        VdcReturnValueBase testResult2 = new VdcReturnValueBase();
        List<VdcReturnValueBase> allResults = new ArrayList<>();
        allResults.add(testResult1);
        allResults.add(testResult2);
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have one results", 1, result.size()); //$NON-NLS-1$
        assertEquals("Result should match", result.get(0), testResult2); //$NON-NLS-1$
    }

    @Test
    public void testTransmitOperationList_oneAction_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runAction(eq(VdcActionType.ActivateVds), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testResult);
    }

    @Test
    public void testTransmitOperationList_oneAction_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runAction(eq(VdcActionType.ActivateVds), eq(testParameters), actionCallback.capture());
        Exception testException = new Exception("Failure"); //$NON-NLS-1$
        actionCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackSingle1).onFailure(testOperation1, testException);
    }

    @Test
    public void testTransmitOperationList_twoItems_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        List<VdcActionParametersBase> testParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(VdcActionType.ActivateVds),
                (ArrayList<VdcActionParametersBase>) eq(testParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        verify(mockOperationCallbackList1).onSuccess(eq(testList), eq(testResultList));
    }

    @Test
    public void testTransmitOperationList_twoItems_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        List<VdcActionParametersBase> testParameterList = createActionParameterList(testParameters, 2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(VdcActionType.ActivateVds),
                (ArrayList<VdcActionParametersBase>) eq(testParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        Exception testException = new Exception("Failure"); //$NON-NLS-1$
        actionCallbackList.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onFailure(eq(testList), eq(testException));
    }

    @Test
    public void testTransmitOperationList_threeItems_twoActionTypes_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> activateVdsList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation3 =
                new VdcOperation<>(VdcActionType.ActivateStorageDomain, testParameters, mockOperationCallbackSingle2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testList.add(testOperation3);
        activateVdsList.add(testOperation1);
        activateVdsList.add(testOperation2);
        List<VdcActionParametersBase> activateVdsParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 3);
        List<VdcReturnValueBase> activateVdsResultList = createActionResultList(testResult, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(VdcActionType.ActivateVds),
                (ArrayList<VdcActionParametersBase>) eq(activateVdsParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        verify(mockService).runAction(eq(VdcActionType.ActivateStorageDomain), eq(testParameters),
                actionCallback.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        actionCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackList1).onSuccess(eq(activateVdsList), eq(activateVdsResultList));
        verify(mockOperationCallbackSingle2).onSuccess(testOperation3, testResultList.get(2));
    }

    @Test
    public void testTransmitOperationList_threeItems_twoActionTypes_one_success_one_failure() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> activateVdsList = new ArrayList<>();
        VdcActionParametersBase testParameters = new VdcActionParametersBase();
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation1 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation3 =
                new VdcOperation<>(VdcActionType.ActivateStorageDomain, testParameters, mockOperationCallbackSingle2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testList.add(testOperation3);
        activateVdsList.add(testOperation1);
        activateVdsList.add(testOperation2);
        List<VdcActionParametersBase> activateVdsParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 3);
        List<VdcReturnValueBase> activateVdsResultList = createActionResultList(testResult, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(VdcActionType.ActivateVds),
                (ArrayList<VdcActionParametersBase>) eq(activateVdsParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        verify(mockService).runAction(eq(VdcActionType.ActivateStorageDomain), eq(testParameters),
                actionCallback.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        actionCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onSuccess(eq(activateVdsList), eq(activateVdsResultList));
        verify(mockOperationCallbackSingle2).onFailure(testOperation3, testException);
    }

    @Test
    public void testTransmitOperationList_oneQuery_success() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), queryCallback.capture());
        VdcQueryReturnValue testResult = new VdcQueryReturnValue();
        queryCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testResult);
    }

    @Test
    public void testTransmitOperationList_oneQuery_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testParameters), queryCallback.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        queryCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackSingle1).onFailure(testOperation1, testException);
    }

    @Test
    public void testTransmitOperationList_multipleQuery_different_callback_success() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> operation1List = new ArrayList<>();
        List<VdcOperation<?, ?>> operation2List = new ArrayList<>();
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation2 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackList2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        operation1List.add(testOperation1);
        operation2List.add(testOperation2);
        List<VdcQueryParametersBase> testParameterList = createQueryParameterList(testParameters, 2);
        List<VdcQueryType> testQueryList = createQueryList(VdcQueryType.Search, 2);
        testProvider.transmitOperationList(testList);
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        List<VdcQueryReturnValue> resultList = createQueryResultList(returnValue, 2);
        List<VdcQueryReturnValue> return1List = createQueryResultList(returnValue, 1);
        List<VdcQueryReturnValue> return2List = createQueryResultList(returnValue, 1);
        verify(mockService).runMultipleQueries(eq((ArrayList<VdcQueryType>) testQueryList),
                (ArrayList<VdcQueryParametersBase>) eq(testParameterList), queryCallbackList.capture());
        queryCallbackList.getValue().onSuccess((ArrayList<VdcQueryReturnValue>) resultList);
        verify(mockOperationCallbackList1).onSuccess(eq(operation1List), eq(return1List));
        verify(mockOperationCallbackList2).onSuccess(eq(operation2List), eq(return2List));
    }

    @Test
    public void testTransmitOperationList_multipleQuery_different_callback_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> operation1List = new ArrayList<>();
        List<VdcOperation<?, ?>> operation2List = new ArrayList<>();
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackList1);
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation2 =
                new VdcOperation<>(VdcQueryType.Search, testParameters, mockOperationCallbackList2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        operation1List.add(testOperation1);
        operation2List.add(testOperation2);
        List<VdcQueryParametersBase> testParameterList = createQueryParameterList(testParameters, 2);
        List<VdcQueryType> testQueryList = createQueryList(VdcQueryType.Search, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleQueries(eq((ArrayList<VdcQueryType>) testQueryList),
                (ArrayList<VdcQueryParametersBase>) eq(testParameterList), queryCallbackList.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        queryCallbackList.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onFailure(eq(operation1List), eq(testException));
        verify(mockOperationCallbackList2).onFailure(eq(operation2List), eq(testException));
    }

    @Test
    public void testTransmitOperationList_query_and_action_success() {
        VdcQueryParametersBase testQueryParameters = new VdcQueryParametersBase();
        VdcActionParametersBase testActionParameters = new VdcActionParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testQueryParameters, mockOperationCallbackSingle1);
        VdcOperation<VdcActionType, VdcActionParametersBase> testOperation2 =
                new VdcOperation<>(VdcActionType.ActivateVds, testActionParameters, mockOperationCallbackSingle2);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runQuery(eq(VdcQueryType.Search), eq(testQueryParameters), queryCallback.capture());
        VdcQueryReturnValue testQueryResult = new VdcQueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);
        VdcReturnValueBase testActionResult = new VdcReturnValueBase();
        verify(mockService).runAction(eq(VdcActionType.ActivateVds), eq(testActionParameters),
                actionCallback.capture());
        actionCallback.getValue().onSuccess(testActionResult);
        verify(mockOperationCallbackSingle2).onSuccess(testOperation2, testActionResult);
    }

    @Test
    public void testTransmitPublicOperationList_success() {
        VdcQueryParametersBase testQueryParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testQueryParameters, true, false, mockOperationCallbackSingle1);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runPublicQuery(eq(VdcQueryType.Search), eq(testQueryParameters), queryCallback.capture());
        VdcQueryReturnValue testQueryResult = new VdcQueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);
    }

    @Test
    public void testTransmitPublicOperationList_two_public_success() {
        VdcQueryParametersBase testQueryParameters = new VdcQueryParametersBase();
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation1 =
                new VdcOperation<>(VdcQueryType.Search, testQueryParameters, true, false, mockOperationCallbackSingle1);
        VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation2 =
                new VdcOperation<>(VdcQueryType.GetConfigurationValues, testQueryParameters, true, false, mockOperationCallbackSingle2);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runPublicQuery(eq(VdcQueryType.Search), eq(testQueryParameters), queryCallback.capture());
        VdcQueryReturnValue testQueryResult = new VdcQueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);

        verify(mockService).runPublicQuery(eq(VdcQueryType.GetConfigurationValues),
                eq(testQueryParameters),
                queryCallback.capture());
        testQueryResult = new VdcQueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle2).onSuccess(testOperation2, testQueryResult);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMissingXsrfToken() {
        //Remove token so there should be a request for it.
        mockXsrfRpcRequestBuilder.setXsrfToken(null);
        VdcQueryParametersBase testParameters = new VdcQueryParametersBase();
        final List<VdcOperation<VdcQueryType, VdcQueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<VdcQueryType, VdcQueryParametersBase> testOperation =
                new VdcOperation<>(VdcQueryType.Search, testParameters, null);
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockXsrfService).getNewXsrfToken((AsyncCallback<XsrfToken>) any());
    }

    // ********************************************************************************************************
    // * Helper functions
    // ********************************************************************************************************
    private List<VdcQueryType> createQueryList(final VdcQueryType queryType, final int count) {
        List<VdcQueryType> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(queryType);
        }
        return result;
    }

    private List<VdcQueryParametersBase> createQueryParameterList(final VdcQueryParametersBase parameters,
            final int count) {
        ArrayList<VdcQueryParametersBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(parameters);
        }
        return result;
    }

    private List<VdcQueryReturnValue> createQueryResultList(final VdcQueryReturnValue resultValue, int count) {
        List<VdcQueryReturnValue> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(resultValue);
        }
        return result;
    }

    private List<VdcActionParametersBase> createActionParameterList(final VdcActionParametersBase parameters,
            final int count) {
        List<VdcActionParametersBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(parameters);
        }
        return result;
    }

    private List<VdcReturnValueBase> createActionResultList(final VdcReturnValueBase resultValue, final int count) {
        List<VdcReturnValueBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(resultValue);
        }
        return result;
    }
}

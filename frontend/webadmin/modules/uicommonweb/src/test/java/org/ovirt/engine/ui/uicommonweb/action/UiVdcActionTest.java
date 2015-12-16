package org.ovirt.engine.ui.uicommonweb.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.ui.uicommonweb.action.UiAction.ActionFlowState;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class UiVdcActionTest extends AsyncUiActionTest<IFrontendActionAsyncCallback> {

    @Test
    public void runNextActionFlowWithFirstActionFailureTest() {
        List<UiAction> actions = runActionFlow(ActionType.next);
        ActionFlowState flowState = actions.get(0).getActionFlowState();

        verifyRunActionAndExecuteCallbacksRandomly(false, flowState, 1);
        verifyRunAction(1);

        // Only actions that were actually executed should be passed
        assertFinishedWithErrors(Collections.singletonList(actions.get(0)), 1);
    }

    @Test
    public void runMixedActionFlowWithFailureOnDependentAction() {
        // action1.and(action2).next(action3).and(action4)
        // action1 and action 2 has failure -> action3 and action4 won't be executed

        List<UiAction> actions = runActionFlow(ActionType.parallel, ActionType.next, ActionType.parallel);
        ActionFlowState flowState = actions.get(0).getActionFlowState();

        verifyRunActionAndExecuteCallbacksRandomly(false, flowState, 2);
        verifyRunAction(2);

        assertFinishedWithErrors(actions.subList(0, 2), 2);
    }

    @Test
    public void showErrorDialogTest() {
        UiAction action = createAction(true);
        action.runAction();

        verifyRunActionAndExecuteCallbacksRandomly(false, action.getActionFlowState(), 1, true);

        assertFinishedWithNoErrors(Collections.singletonList(action));
    }

    @Override
    protected UiVdcAction createAction() {
        return new UiVdcAction(ACTION_TYPE, new VdcActionParametersBase(), model);
    }

    private UiVdcAction createAction(boolean showErrorDialog) {
        return new UiVdcAction(ACTION_TYPE, new VdcActionParametersBase(), model, showErrorDialog);
    }

    private void verifyRunActionAndExecuteCallbacksRandomly(boolean success,
            ActionFlowState flowState,
            int exepectedNumOfRunActionExecutions, boolean showErrorDialog) {
        verifyRunActionAndExecuteCallbacksRandomly(success,
                flowState,
                exepectedNumOfRunActionExecutions,
                exepectedNumOfRunActionExecutions,
                showErrorDialog);
    }

    private void verifyRunActionAndExecuteCallbacksRandomly(boolean success,
            ActionFlowState flowState,
            int exepectedNumOfRunActionExecutions, int numOfCallbacksFromTheEndToExecute, boolean showErrorDialog) {
        List<IFrontendActionAsyncCallback> callbacks =
                verfifyRunAction(exepectedNumOfRunActionExecutions, showErrorDialog);
        executeCallbacks(success,
                flowState,
                callbacks.subList(callbacks.size() - numOfCallbacksFromTheEndToExecute, callbacks.size()));
    }

    @Override
    protected void verifyRunActionAndExecuteCallbacksRandomly(boolean success,
            ActionFlowState flowState,
            int exepectedNumOfRunActionExecutions, int numOfCallbacksFromTheEndToExecute) {
        verifyRunActionAndExecuteCallbacksRandomly(success,
                flowState,
                exepectedNumOfRunActionExecutions,
                numOfCallbacksFromTheEndToExecute,
                false);
    }

    @Override
    protected void executeCallbacks(boolean success,
            ActionFlowState flowState,
            List<IFrontendActionAsyncCallback> callbacks) {
        Collections.shuffle(callbacks);

        for (IFrontendActionAsyncCallback callback : callbacks) {
            assertNotAllDone(flowState);
            VdcReturnValueBase result = new VdcReturnValueBase();
            result.setValid(true);
            result.setSucceeded(success);
            callback.executed(new FrontendActionAsyncResult(ACTION_TYPE, null, result));

        }
    }

    private List<IFrontendActionAsyncCallback> verfifyRunAction(int exepectedNumOfRunActionExecutions,
            boolean showErrorDialog) {
        verify(frontend, times(exepectedNumOfRunActionExecutions)).runAction(eq(ACTION_TYPE),
                any(VdcActionParametersBase.class),
                callbackCaptor.capture(),
                eq(showErrorDialog));
        List<IFrontendActionAsyncCallback> callbacks = callbackCaptor.getAllValues();
        return callbacks;
    }

    @Override
    protected List<IFrontendActionAsyncCallback> verifyRunAction(int exepectedNumOfRunActionExecutions) {
        return verfifyRunAction(exepectedNumOfRunActionExecutions, false);
    }
}

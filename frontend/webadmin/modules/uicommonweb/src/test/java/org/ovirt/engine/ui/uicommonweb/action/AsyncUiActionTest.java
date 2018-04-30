package org.ovirt.engine.ui.uicommonweb.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.action.UiAction.ActionFlowState;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

@ExtendWith(UiCommonSetupExtension.class)
public abstract class AsyncUiActionTest<C> extends UiActionBaseTest {

    protected org.ovirt.engine.core.common.action.ActionType
            ACTION_TYPE = org.ovirt.engine.core.common.action.ActionType.Unknown;

    @Captor
    protected ArgumentCaptor<C> callbackCaptor;

    protected Frontend frontend;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        frontend = Frontend.getInstance(); // Mocked by UiCommonSetupExtension
    }

    @Test
    public void runSingleActionWithSuccessTest() {
        UiAction action = runSingleActionCommon(true);
        assertFinishedWithNoErrors(Collections.singletonList(action));
    }

    @Test
    public void runSingleActionWithFailureTest() {
        UiAction action = runSingleActionCommon(false);
        assertFinishedWithErrors(Collections.singletonList(action), 1);
    }

    private UiAction runSingleActionCommon(boolean succeed) {
        UiAction action = createAction();
        action.runAction();

        verifyRunActionAndExecuteCallbacksRandomly(succeed, action.getActionFlowState(), 1);
        return action;
    }

    @Test
    public void runParallelActionFlowAllSucceedTest() {
        List<UiAction> actions = runParallelActionFlowCommon(true);

        assertFinishedWithNoErrors(actions);
    }

    @Test
    public void runParallelActionFlowWithFailureTest() {
        List<UiAction> actions = runParallelActionFlowCommon(false);

        assertFinishedWithErrors(actions, actions.size());
    }

    private List<UiAction> runParallelActionFlowCommon(boolean success) {
        List<UiAction> actions = runActionFlow(UiActionBaseTest.ActionType.parallel);

        verifyRunActionAndExecuteCallbacksRandomly(success, actions.get(0).getActionFlowState(), actions.size());

        return actions;
    }

    @Test
    public void runNextActionFlowAllSucceedTest() {
        List<UiAction> actions = runNextActionFlowCommon(true);
        assertFinishedWithNoErrors(actions);
    }

    @Test
    public void runNextActionFlowWithLastActionFailureTest() {
        List<UiAction> actions = runNextActionFlowCommon(false);
        assertFinishedWithErrors(actions, 1);
    }

    private List<UiAction> runNextActionFlowCommon(boolean lastSuccess) {
        List<UiAction> actions = runActionFlow(UiActionBaseTest.ActionType.next);
        ActionFlowState flowState = actions.get(0).getActionFlowState();

        verifyRunActionAndExecuteCallbacksRandomly(true, flowState, 1);
        verifyRunActionAndExecuteCallbacksRandomly(lastSuccess, flowState, 2, 1);

        return actions;
    }

    @Test
    public void runMixedActionFlowAllSucceed() {
        // action1.and(action2).next(action3).and(action4)
        List<UiAction> actions = runActionFlow(UiActionBaseTest.ActionType.parallel, UiActionBaseTest.ActionType.next, UiActionBaseTest.ActionType.parallel);
        ActionFlowState flowState = actions.get(0).getActionFlowState();

        verifyRunActionAndExecuteCallbacksRandomly(true, flowState, 2);
        verifyRunActionAndExecuteCallbacksRandomly(true, flowState, 4, 2);

        assertFinishedWithNoErrors(actions);
    }

    @Test
    public void runMixedActionFlowWithFailureOnIndependentAction() {
        // action1.and(action2).next(action3).and(action4)
        // action1 has an error- all the flow will be executed

        List<UiAction> actions = runActionFlow(UiActionBaseTest.ActionType.parallel, UiActionBaseTest.ActionType.next, UiActionBaseTest.ActionType.parallel);
        ActionFlowState flowState = actions.get(0).getActionFlowState();

        List<C> callbacks = verifyRunAction(2);

        // execute action1 callback
        executeCallbacks(false, flowState, callbacks.subList(1, 2));

        // execute action2 callback
        executeCallbacks(true, flowState, callbacks.subList(0, 1));

        // verify action3 and action4 were also executed
        callbacks = verifyRunAction(4);

        // executed the callbacks of action3 and action4
        executeCallbacks(true, flowState, callbacks.subList(callbacks.size() - 2, callbacks.size()));

        assertFinishedWithErrors(actions, 1);
    }

    @Test
    public void actionWithParallelAndNextActions() {
        UiAction action = createAction();
        UiAction parallelAction = createAction();
        UiAction nextAction = createAction();

        action.and(parallelAction);
        action.then(nextAction);
        action.runAction();

        verifyRunActionAndExecuteCallbacksRandomly(true, action.getActionFlowState(), 2);
        verifyRunActionAndExecuteCallbacksRandomly(true, action.getActionFlowState(), 3, 1);

        assertFinishedWithNoErrors(new ArrayList<UiAction>(Arrays.asList(action, parallelAction, nextAction)));
    }

    /**
     * Executes the callbacks passed as a parameter.
     *
     * @param success
     *            the success value that should be set on the return value of the callbacks
     * @param flowState
     *            he flow state of the actions which are under test (is used to verify that the flow is not marked as
     *            done before all the excepted actions and callbacks were executed)
     * @param callbacks
     *            the number of <code>Frontend.runAction(..)</code> executions excepted up to this point
     */
    protected abstract void executeCallbacks(boolean success,
            ActionFlowState flowState,
            List<C> callbacks);

    /**
     * Composition of <code>verifyRunAction(..)</code> and <code>executeCallbacks(..)</code> which executes all the
     * callbacks that were returned by <code>verifyRunAction(..)</code>.
     *
     * @param success
     *            the success value that should be set on the return value of the callbacks
     * @param flowState
     *            the flow state of the actions which are under test (is used to verify that the flow is not marked as
     *            done before all the excepted actions and callbacks were executed)
     * @param exepectedNumOfRunActionExecutions
     *            the number of <code>Frontend.runAction(..)</code> executions excepted up to this point
     * @param numOfCallbacksFromTheEndToExecute
     *            the number of callbacks that should be executed starting at the end of the callbacks list (it is
     *            useful since <code>verifyRunAction(..)</code> returns the callbacks of <b>all</b> the
     *            <code>Frontend.runAction(..)</code>s that were executed so far. In case the callbacks of the first
     *            runActions were already executed, just the callbacks of the newly executed runActions should be
     *            executed).
     */
    protected abstract void verifyRunActionAndExecuteCallbacksRandomly(boolean success,
            ActionFlowState flowState,
            int exepectedNumOfRunActionExecutions, int numOfCallbacksFromTheEndToExecute);

    protected void verifyRunActionAndExecuteCallbacksRandomly(boolean success,
            ActionFlowState flowState,
            int exepectedNumOfRunActionExecutions) {
        verifyRunActionAndExecuteCallbacksRandomly(success,
                flowState,
                exepectedNumOfRunActionExecutions,
                exepectedNumOfRunActionExecutions);
    }

    /**
     * Verifies that <code>Frontend.runAction(..)</code> was executed <code>exepectedNumOfRunActionExecutions</code>
     * times.
     *
     * @return the callbacks of all the <code>Frontend.runAction(..)</code> that were executed.
     */
    protected abstract List<C> verifyRunAction(int exepectedNumOfRunActionExecutions);
}

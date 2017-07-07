package org.ovirt.engine.ui.uicommonweb.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * This class represents an action. The action is executed by invoking <code>runAction()</code>. A content is added to
 * the action by implementing <code>onActionExecuted</code>.
 *
 * <code>nextAction</code> specified by <code>then(UiAction action)</code> will run just after the source action has
 * been executed. <code>parallelAction</code> specified by <code>and(UiAction action)</code> will run before the source
 * action is executed (is not dependent of the source action execution).
 *
 * <code>then(..)</code> and <code>and(..)</code> methods allows creating a flow of actions.
 *
 * For example-
 * <code>actionA.then(actionB).and(actionC).and(actionD).then(actionE).and(actionF).onAllExecutionsFinish(actionG)</code>
 *
 * <code>model.startProgress(null)</code> is called when the first action in the flow is executed.
 * <code>model.stopProgress()</code> is called after the last action in the flow is executed.
 *
 * The errors from all the actions in the flow can be collected and displayed after the last action in the flow is
 * executed.
 *
 * <code>ActionFlowState</code> is shared between all the action in the flow.
 */
public abstract class UiAction {

    private UiAction nextAction;
    private SimpleAction finalAction;
    private Model model;
    private UiAction parallelAction;
    private ActionFlowState actionFlowState;
    private String name;

    /**
     * @param model
     *            the model on which to start and stop the progress.
     */
    public UiAction(Model model) {
        this(model, ""); //$NON-NLS-1$
    }

    /**
     * @param model
     *            the model on which to start and stop the progress.
     * @param name
     *            action name
     */
    public UiAction(Model model, String name) {
        this.model = model;
        this.name = name;
    }

    /**
     * The execution of the action should be done by calling this method.
     */
    public final void runAction() {
        if (actionFlowState == null) {
            actionFlowState = new ActionFlowState(1, finalAction);
        }

        if (model.getProgress() == null) {
            model.startProgress();
            actionFlowState.setStartedProgress(true);
        }

        if (parallelAction != null) {
            parallelAction.runParallelAction(actionFlowState);
        }

        if (shouldExecute()) {
            internalRunAction();
        } else {
            runNextAction();
        }
    }

    private final void runAction(ActionFlowState actionFlowState) {
        this.actionFlowState = actionFlowState;
        if (finalAction != null) {
            actionFlowState.setFinalAction(finalAction);
        }
        runAction();
    }

    /**
     * This method is used if the action should be added to an existing actions flow. The meaning of adding to an
     * existing flow is that for example <code>model.stopProgress()</code> and the <code>finishAction</code> will be
     * executed just after all the actions in the flow (including this one) will be finished.
     *
     * @param actionFlowState
     *            the state of the flow this action wants to join.
     */
    public void runParallelAction(ActionFlowState actionFlowState) {
        actionFlowState.incBranchCounter();
        runAction(actionFlowState);
    }

    abstract void internalRunAction();

    /**
     * Specifying an action that will run immediately after <code>this</code> action has finished its execution.
     */
    public UiAction then(UiAction nextAction) {
        this.nextAction = nextAction;
        return nextAction;
    }

    /**
     * Specifying an action that will run before <code>this</code> action has finished its execution, and is not
     * dependent on <code>this</code> action's execution.
     */
    public UiAction and(UiAction parallelAction) {
        this.parallelAction = parallelAction;
        return parallelAction;
    }

    /**
     * The <code>finalAction</code> will be executed when the flow is completed (and reached the
     * current action).
     *
     * For example-
     * 1. action1.then(action2).onAllExecutionsFinish(finalAction)
     * if action1 fails, the finalAction won't be executed (since the flow didn't reach the action it was set on).
     * if action1 succeeds, the finalAction will be executed when the flow is completed whether action2 succeeds or not.
     * 2. (not recommended!) action1.onAllExecutionsFinish(finalAction).then(action2)
     * the final action will be executed whether action1 succeeds or fails when the flow is completed.
     *
     * It is NOT recommended to set the final action in the middle of the flow!
     */
    public void onAllExecutionsFinish(SimpleAction finalAction) {
        this.finalAction = finalAction;
    }

    protected boolean shouldExecute() {
        return true;
    }

    protected UiAction getNextAction() {
        return nextAction;
    }

    void runNextAction() {
        if (getNextAction() != null) {
            getNextAction().runAction(actionFlowState);
        } else {
            actionFlowState.decBranchCounter();
            tryToFinalize(true);
        }
    }

    public Model getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    void tryToFinalize(boolean handleErrors) {
        if (actionFlowState.isAllDone()) {
            if (actionFlowState.isStartedProgress()) {
                model.stopProgress();
            }

            if (handleErrors) {
                handleErrors();
            }

            if (actionFlowState.getFinalAction() != null) {
                actionFlowState.getFinalAction().execute();
            }
        }
    }

    private void handleErrors() {
        if (!getActionFlowState().getFailedActionsMap().isEmpty()) {
            Frontend.getInstance().runMultipleActionsFailed(getActionFlowState().getFailedActionsMap(),
                    innerMessage -> ConstantsManager.getInstance()
                            .getMessages()
                            .uiCommonRunActionPartitialyFailed(innerMessage));
        }
    }

    protected ActionFlowState getActionFlowState() {
        return actionFlowState;
    }

    protected static class ActionFlowState {
        private int branchCounter;
        private SimpleAction finalAction;
        Map<ActionType, List<ActionReturnValue>> failedActionsMap = new HashMap<>();
        private boolean startedProgress;

        public ActionFlowState(int value, SimpleAction finalAction) {
            this.branchCounter = value;
            this.finalAction = finalAction;
        }

        protected void incBranchCounter() {
            this.branchCounter++;
        }

        protected void decBranchCounter() {
            this.branchCounter--;
        }

        boolean isAllDone() {
            return branchCounter == 0;
        }

        private void setFinalAction(SimpleAction finalAction) {
            if (this.finalAction != null) {
                throw new UnsupportedOperationException("A final action was already set on the flow"); //$NON-NLS-1$
            }
            this.finalAction = finalAction;
        }

        private SimpleAction getFinalAction() {
            return finalAction;
        }

        protected void addFailure(ActionType actionType, ActionReturnValue result) {
            List<ActionReturnValue> actionTypeResults = failedActionsMap.get(actionType);
            if (actionTypeResults == null) {
                actionTypeResults = new LinkedList<>();
                failedActionsMap.put(actionType, actionTypeResults);
            }

            actionTypeResults.add(result);
        }

        protected Map<ActionType, List<ActionReturnValue>> getFailedActionsMap() {
            return failedActionsMap;
        }

        public boolean isStartedProgress() {
            return startedProgress;
        }

        public void setStartedProgress(boolean startedProgress) {
            this.startedProgress = startedProgress;
        }
    }
}

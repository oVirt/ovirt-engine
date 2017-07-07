package org.ovirt.engine.ui.uicommonweb.action;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

/**
 * This class represents an action that runs multiple vdcActions
 * <code>Frontend.getInstance().runMultipleAction(..)</code>.
 *
 * The next action will be executed just after the multiple action's result is returned. A parallel action will be
 * invoked at the same time as this action.
 *
 * For example-
 * <code>actionA.then(actionB).and(actionC).and(actionD).then(actionE).and(actionF).onAllExecutionsFinish(actionG)</code>
 * <code>actionA.runAction()</code>
 *
 * Since the vdc actions are asynchronous the flow will be the following-
 * 1. actionA will execute a multiple vdcAction.
 * 2. actionB, actionC and actionD will be executed in a parallel manner just after the result of actionA is returned.
 * 3. actionE/F will be executed parallely after actionD's result is returned.
 * 4. actionG is the <code>final</code>, it will be executed just after the result of all the action's in the flow was returned.
 *
 * actionA->actionB ->actionC ->actionD->actionE ->actionF ->actionG
 *
 * The failures are collected and displayed after finishing the execution of the flow.
 */
public class UiVdcMultipleAction extends UiAction {

    private ActionType actionType;
    private Collection<? extends ActionParametersBase> parameterCollection;
    boolean waitForResult;
    boolean runNextInCaseOfError;

    /**
     * @param actionType
     *            the <code>VdsActionType</code>
     * @param parameterCollection
     *            the parameters of the multiple actions
     * @param model
     *            model the model on which to start and stop the progress.
     * @param waitForResult
     *            a flag to return the result after running the whole action and not just the can do actions.
     * @param runNextInCaseOfError
     *            whether the next action should be executed in case the current action has an error.
     */
    public UiVdcMultipleAction(ActionType actionType,
            Collection<? extends ActionParametersBase> parameterCollection,
            Model model,
            boolean waitForResult,
            boolean runNextInCaseOfError) {
        super(model);
        this.actionType = actionType;
        this.parameterCollection = parameterCollection;
        this.waitForResult = waitForResult;
        this.runNextInCaseOfError = runNextInCaseOfError;
    }

    public UiVdcMultipleAction(ActionType actionType,
            Collection<? extends ActionParametersBase> parametersList,
            Model model) {
        this(actionType, parametersList, model, false, true);
    }

    @Override
    void internalRunAction() {
        Frontend.getInstance().runMultipleAction(actionType,
                new ArrayList<>(parameterCollection),
                createCallback(),
                false,
                waitForResult);
    }

    private IFrontendMultipleActionAsyncCallback createCallback() {
        return result -> {
            boolean hasError = false;

            for (ActionReturnValue singleResult : result.getReturnValue()) {
                if (!singleResult.isValid() || (waitForResult && !singleResult.getSucceeded())) {
                    hasError = true;
                    getActionFlowState().addFailure(actionType, singleResult);
                }
            }

            if (hasError && !runNextInCaseOfError) {
                then(null);
            }

            runNextAction();
        };
    }

    @Override
    protected boolean shouldExecute() {
        return super.shouldExecute() && parameterCollection != null && !parameterCollection.isEmpty();
    }
}
